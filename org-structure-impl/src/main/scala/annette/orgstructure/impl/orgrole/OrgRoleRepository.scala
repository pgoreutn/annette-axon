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

import annette.orgstructure.api.model.{OrgRole, OrgRoleId}
import com.datastax.driver.core.Row
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.sksamuel.elastic4s.ElasticClient
import play.api.Configuration

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

private[impl] class OrgRoleRepository(session: CassandraSession)(
    implicit val ec: ExecutionContext
) {
  def getOrgRoleById(id: OrgRoleId): Future[Option[OrgRole]] = {
    for {
      stmt <- session.prepare("SELECT * FROM org_roles WHERE id = ?")
      result <- session.selectOne(stmt.bind(id)).map(_.map(convertOrgRole))
    } yield result
  }

  def getOrgRolesByIds(ids: Set[OrgRoleId]): Future[Set[OrgRole]] = {
    for {
      stmt <- session.prepare("SELECT * FROM org_roles WHERE id IN ?")
      result <- session.selectAll(stmt.bind(ids.toList.asJava)).map(_.map(convertOrgRole))
    } yield result.to[Set]
  }

  private def convertOrgRole(row: Row): OrgRole = {
    OrgRole(
      id = row.getString("id"),
      name = row.getString("name"),
      description = Option(row.getString("description")),
      updatedAt = OffsetDateTime.parse(row.getString("updated_at")),
      active = row.getBool("active")
    )

  }

}
