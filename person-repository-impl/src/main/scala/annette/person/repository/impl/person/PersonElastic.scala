/*
 * Copyright 2018 Valery Lobachev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package annette.person.repository.impl.person

import java.time.OffsetDateTime

import akka.Done
import annette.person.repository.api.model._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.playjson.playJsonIndexable
import com.sksamuel.elastic4s.requests.analysis._
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.indexes.admin.IndexExistsResponse
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.requests.searches.queries.Query
import com.sksamuel.elastic4s.requests.searches.queries.matches.{FieldWithOptionalBoost, MultiMatchQuery, MultiMatchQueryBuilderType}
import com.sksamuel.elastic4s.requests.searches.sort.{FieldSort, SortOrder}
import org.slf4j.LoggerFactory
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

class PersonElastic(configuration: Configuration, elasticClient: ElasticClient)(implicit val ec: ExecutionContext) {
  private val log = LoggerFactory.getLogger(classOf[PersonElastic])

  private val prefix: String = configuration.getOptional[String]("elastic.prefix").map(_ + "-").getOrElse("")

  private val indexName = s"${prefix}person_service-person"

  implicit private val indexable = playJsonIndexable[PersonIndex]

  def createPersonIndex = {
    elasticClient
      .execute(indexExists(indexName))
      .map {
        case failure: RequestFailure =>
          log.error("createPersonIndex: indexExists validation failed", failure.error)
          false
        case res: RequestSuccess[IndexExistsResponse] =>
          res.result.exists
      }
      .flatMap {
        case true =>
          log.debug("createPersonIndex: Index exists")
          Future.successful(Done)
        case false =>
          log.debug(s"createPersonIndex: Index ${indexName} does not exists")
          val future = elasticClient.execute {
            createIndex(indexName)
              .mapping(
                properties(
                  textField("id"),
                  textField("lastname").analyzer("ngram_name_analyzer").searchAnalyzer("standard"),
                  textField("firstname").analyzer("ngram_name_analyzer").searchAnalyzer("standard"),
                  textField("middlename").analyzer("ngram_name_analyzer").searchAnalyzer("standard"),
                  keywordField("personType"),
                  textField("phone"),
                  textField("email").analyzer("ngram_name_analyzer").searchAnalyzer("standard"),
                  dateField("updatedAt"),
                  booleanField("active")
                )
              )
              .analysis(
                Analysis(
                  analyzers = List(
                    CustomAnalyzer(
                      name = "ngram_name_analyzer",
                      tokenizer = "standard", //StandardTokenizer,
                      charFilters = Nil,
                      tokenFilters = "lowercase" :: "edge_ngram_filter" :: Nil
                    )
                  ),
                  tokenFilters = EdgeNGramTokenFilter("edge_ngram_filter", Some(3), Some(50)) :: Nil
                )
              )
          }
          (for {
            res <- future
          } yield {
            log.debug("createESIndex: success:", res.result.toString)
            Done
          }).recover {
            case th: Throwable =>
              log.error("createESIndex: failure", th)
          }
      }

  }

  def indexPerson(person: Person) = {
    elasticClient.execute {
      indexInto(indexName)
        .id(person.id)
        .doc(PersonIndex(person))
        .refresh(RefreshPolicy.Immediate)
    }
  }

  def findPerson(query: PersonFindQuery): Future[PersonFindResult] = {

    val activeQuery: Seq[Query] =
      if (query.activeOnly) Seq(termQuery("active", true))
      else Seq.empty
    val fieldQuery = Seq(
      query.firstname.map(matchQuery("firstname", _)),
      query.lastname.map(matchQuery("lastname", _)),
      query.middlename.map(matchQuery("middlename", _)),
      query.phone.map(matchQuery("phone", _)),
      query.email.map(matchQuery("email", _)),
      query.personType.map(matchQuery("personType", _))
    ).flatten
    val filterQuery = buildFilterQuery(query)
    val sortQuery = buildSortQuery(query)

    val future = elasticClient.execute {
      search(indexName)
        .query(boolQuery.filter(filterQuery).must(fieldQuery))
        //.filter()
        .from(query.offset)
        .size(query.size)
        .sortBy(sortQuery)
        .sourceInclude("updatedAt")
        .trackTotalHits(true)
    }

    for {
      resp <- future
    } yield {
      resp match {
        case failure: RequestFailure =>
          log.error("findPerson: failed " + failure.error)
          PersonFindResult(0, Seq.empty)
        case results: RequestSuccess[SearchResponse] =>
          log.info(s"findPerson: query ${query.toString},  results ${results.toString}")
          val total = results.result.hits.total.value
          val hits = results.result.hits.hits.map { hit =>
            val updatedAt = hit.sourceAsMap
              .get("updatedAt")
              .map(v => OffsetDateTime.parse(v.toString))
              .getOrElse(OffsetDateTime.now)
            PersonHitResult(hit.id, hit.score, updatedAt)
          }.toSeq
          PersonFindResult(total, hits)
      }
    }

  }

  private def buildFilterQuery(query: PersonFindQuery) = {
    Seq(
      query.filter.map { filterString =>
        val fields = Seq(
          FieldWithOptionalBoost("lastname", Some(3.0)),
          FieldWithOptionalBoost("firstname", Some(2.0)),
          FieldWithOptionalBoost("middlename", None),
          FieldWithOptionalBoost("email", Some(3.0)),
          FieldWithOptionalBoost("phone", Some(1.0))
        )
        dismax(
          Seq(
            MultiMatchQuery(filterString, fields = fields, `type` = Some(MultiMatchQueryBuilderType.CROSS_FIELDS))
          )
        )
      }
    ).flatten
  }
  private def buildSortQuery(query: PersonFindQuery) = {
    query.sortBy
      .map { sortBy =>
        val sortOrder =
          if (query.ascending) SortOrder.Asc
          else SortOrder.Desc
        sortBy match {
          case PersonSortBy.Lastname =>
            Seq(
              FieldSort("lastname", order = sortOrder),
              FieldSort("firstname", order = sortOrder),
              FieldSort("middlename", order = sortOrder)
            )
          case PersonSortBy.Firstname =>
            Seq(
              FieldSort("firstname", order = sortOrder),
              FieldSort("lastname", order = sortOrder),
              FieldSort("middlename", order = sortOrder)
            )
          case PersonSortBy.Email =>
            Seq(
              FieldSort("email", order = sortOrder)
            )
          case PersonSortBy.Phone =>
            Seq(
              FieldSort("phone", order = sortOrder)
            )
        }
      }
      .getOrElse(Seq.empty)
  }
}
