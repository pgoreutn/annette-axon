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

package annette.orgstructure.impl.orgrole

import java.time.OffsetDateTime

import akka.Done
import annette.orgstructure.api.model._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.playjson.playJsonIndexable
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.indexes.CreateIndexResponse
import com.sksamuel.elastic4s.requests.indexes.admin.IndexExistsResponse
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.requests.searches.queries.QueryStringQuery
import com.sksamuel.elastic4s.requests.searches.sort.{FieldSort, SortOrder}
import org.slf4j.LoggerFactory
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

class OrgRoleElastic(configuration: Configuration, elasticClient: ElasticClient)(implicit val ec: ExecutionContext) {
  def deactivateOrgRole(id: OrgRoleId): Future[Unit] = {
    Future.successful()
  }

  def activateOrgRole(id: OrgRoleId): Future[Unit] = {
    Future.successful()
  }

  private val log = LoggerFactory.getLogger(classOf[OrgRoleElastic])

  val prefix: String = configuration.getOptional[String]("elastic.prefix").map(prefix => s"$prefix-").getOrElse("")

  val indexName = s"${prefix}org-structure-org-roles"

  implicit val indexable = playJsonIndexable[OrgRoleIndex]

  def createOrgRolesIndex = {
    elasticClient
      .execute(indexExists(indexName))
      .map {
        case failure: RequestFailure =>
          log.error("createOrgRolesIndex: indexExists validation failed", failure.error)
          false
        case res: RequestSuccess[IndexExistsResponse] =>
          res.result.exists
      }
      .flatMap {
        case true =>
          log.info("createOrgRolesIndex: Index exists")
          Future.successful(Done)
        case false =>
          log.info("createOrgRolesIndex: Index does not exists")
          val future = elasticClient.execute {
            createIndex(indexName)
              .mapping(
                properties(
                  textField("id"),
                  keywordField("name"),
                  dateField("updatedAt"),
                  booleanField("active")
                )
              )
          }
          (for {
            res <- future
          } yield {
            res match {
              case _: RequestSuccess[_] =>
                log.debug("createOrgRolesIndex Success: {}", res.toString)
                Done
              case failure: RequestFailure =>
                log.error("createOrgRolesIndex Failure: {}", failure)
                throw new Exception(failure.error.toString)
            }
          }).recover {
            case th: Throwable =>
              log.error("createOrgRolesIndex Failure", th)
              throw th
          }
      }

  }

  def indexOrgRole(orgRole: OrgRole) = {
    elasticClient.execute {
      indexInto(indexName)
        .id(orgRole.id)
        .doc(OrgRoleIndex(orgRole))
        .refresh(RefreshPolicy.Immediate)
    }
  }

  def deleteOrgRole(id: OrgRoleId) = {
    elasticClient.execute {
      deleteById(indexName, id)
    }
  }

  def findOrgRoles(filter: OrgRoleFilter): Future[OrgRoleFindResult] = {

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
        .sourceInclude("updatedAt")
        .sortBy(sortBy)
        .trackTotalHits(true)
    }

    for {
      resp <- future
    } yield {
      resp match {
        case failure: RequestFailure =>
          log.error("We failed " + failure.error)
          OrgRoleFindResult(0, Seq.empty)
        case results: RequestSuccess[SearchResponse] =>
          log.debug(results.toString)
          val total = results.result.hits.total.value
          val hits = results.result.hits.hits.map { hit =>
            val updatedAt = hit.sourceAsMap
              .get("updatedAt")
              .map(v => OffsetDateTime.parse(v.toString))
              .getOrElse(OffsetDateTime.now)
            OrgRoleHitResult(
              hit.id,
              hit.score,
              updatedAt
            )
          }.toSeq
          OrgRoleFindResult(total, hits)
      }
    }
  }

}
