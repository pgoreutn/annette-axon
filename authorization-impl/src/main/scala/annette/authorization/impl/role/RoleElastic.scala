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

package annette.authorization.impl.role

import akka.Done
import annette.authorization.api.model._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.playjson.playJsonIndexable
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.indexes.admin.IndexExistsResponse
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.requests.searches.queries.QueryStringQuery
import com.sksamuel.elastic4s.requests.searches.sort.{FieldSort, SortOrder}
import org.slf4j.LoggerFactory
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

class RoleElastic(configuration: Configuration, elasticClient: ElasticClient)(implicit val ec: ExecutionContext) {
  private val log = LoggerFactory.getLogger(classOf[RoleElastic])

  val prefix: String = configuration.getOptional[String]("elastic.prefix").map(prefix => s"$prefix-").getOrElse("")

  val indexName = s"${prefix}authorization-roles"

  implicit val indexable = playJsonIndexable[RoleIndex]

  def createRolesIndex = {
    elasticClient
      .execute(indexExists(indexName))
      .map {
        case failure: RequestFailure =>
          log.error("createRolesIndex: indexExists validation failed", failure.error)
          false
        case res: RequestSuccess[IndexExistsResponse] =>
          res.result.exists
      }
      .flatMap {
        case true =>
          log.info("createRolesIndex: Index exists")
          Future.successful(Done)
        case false =>
          log.info("createRolesIndex: Index does not exists")
          val future = elasticClient.execute {
            createIndex(indexName)
              .mapping(
                properties(
                  textField("id"),
                  keywordField("name")
                )
              )
          }
          (for {
            res <- future
          } yield {
            log.info("createRolesIndex Success")
            log.info(res.toString)
            Done
          }).recover {
            case th: Throwable =>
              log.error("createRolesIndex Failure", th)
          }
      }

  }

  def indexRole(role: Role) = {
    elasticClient.execute {
      indexInto(indexName)
        .id(role.id)
        .doc(RoleIndex(role))
        .refresh(RefreshPolicy.Immediate)
    }
  }

  def deleteRole(id: RoleId) = {
    elasticClient.execute {
      deleteById(indexName, id)
    }
  }

  def findRoles(filter: RoleFilter): Future[RoleFindResult] = {

    val nameQuery = filter.filterByName
      .map(filterByName => QueryStringQuery(defaultField = Some("name"), query = s"*$filterByName*"))

    val sortBy = filter.sortBy
      .map { field =>
        val sortOrder = if (filter.ascending) SortOrder.Asc else SortOrder.Desc
        Seq(FieldSort(field, order = sortOrder))
      }
      .getOrElse(Seq.empty)

    val future = elasticClient.execute {
      search(indexName)
        .bool(must(nameQuery))
        .from(filter.offset)
        .size(filter.size)
        .fetchSource(false)
        .sortBy(sortBy)
    }

    for {
      resp <- future
    } yield {
      resp match {
        case failure: RequestFailure =>
          log.error("We failed " + failure.error)
          RoleFindResult(0, Seq.empty)
        case results: RequestSuccess[SearchResponse] =>
          log.debug(results.toString)
          val total = results.result.hits.total.value
          val hits = results.result.hits.hits.map { hit =>
            RoleHitResult(
              hit.id,
              hit.score
            )
          }.toSeq
          RoleFindResult(total, hits)
      }
    }
  }

}
