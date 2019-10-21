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

import akka.Done
import annette.orgstructure.api.model.OrgRole
import com.datastax.driver.core.PreparedStatement
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import org.slf4j.LoggerFactory
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

private[impl] class OrgRoleEventProcessor(
                                        session: CassandraSession,
                                        readSide: CassandraReadSide,
                                        elastic: OrgRoleElastic,
                                        val configuration: Configuration,
)(implicit val ec: ExecutionContext)
    extends ReadSideProcessor[OrgRoleEvent] {
  private val log = LoggerFactory.getLogger(classOf[OrgRoleEventProcessor])

  private var insertOrgRoleStatement: PreparedStatement = null
  private var updateOrgRoleStatement: PreparedStatement = null
  private var deactivateOrgRoleStatement: PreparedStatement = null
  private var activateOrgRoleStatement: PreparedStatement = null

  def buildHandler = {
    readSide
      .builder[OrgRoleEvent]("orgRoleEventOffset")
      .setGlobalPrepare(globalPrepare)
      .setPrepare(_ => prepareStatements())
      .setEventHandler[OrgRoleCreated](e => insertOrgRole(e.event.orgRole))
      .setEventHandler[OrgRoleUpdated](e => updateOrgRole(e.event.orgRole))
      .setEventHandler[OrgRoleDeactivated](e => deactivateOrgRole(e.event))
      .setEventHandler[OrgRoleActivated](e => activateOrgRole(e.event))
      .build
  }

  def aggregateTags = OrgRoleEvent.Tag.allTags

  private def globalPrepare() = {
    for {
      esRes <- elastic.createOrgRolesIndex
      _ <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS org_roles (
          id text PRIMARY KEY,
          name text,
          description text,
          updated_at text,
          active boolean
        )
      """)

    } yield {
      log.debug(esRes.toString)
      Done
    }
  }

  private def prepareStatements() = {
    // гарантирует инициализацию репозитория

    for {
      insertOrgRole <- session.prepare("""
        INSERT INTO org_roles(id, name, description, updated_at, active)
          VALUES (:id, :name, :description, :updated_at, :active)
      """)
      updateOrgRole <- session.prepare("""
        UPDATE org_roles SET name = :name, description = :description, updated_at = :updated_at, active = :active WHERE id = ?
      """)
      deactivateOrgRole <- session.prepare("""
         UPDATE org_roles SET active = false WHERE id = ?
      """)
      activateOrgRole <- session.prepare("""
         UPDATE org_roles SET active = true WHERE id = ?
      """)

    } yield {
      insertOrgRoleStatement = insertOrgRole
      updateOrgRoleStatement = updateOrgRole
      deactivateOrgRoleStatement = deactivateOrgRole
      activateOrgRoleStatement = activateOrgRole
      Done
    }
  }

  private def insertOrgRole(orgRole: OrgRole) = {

    for {
      esRes <- elastic.indexOrgRole(orgRole)
    } yield {
      log.debug(s"insertOrgRole: ${esRes.toString}")
      List(
        insertOrgRoleStatement
          .bind()
          .setString("id", orgRole.id)
          .setString("name", orgRole.name)
          .setString("description",  orgRole.description.getOrElse(""))
          .setString("updated_at",  orgRole.updatedAt.toString)
          .setBool("active", orgRole.active)
      )
    }
  }

  private def updateOrgRole(orgRole: OrgRole) = {
    for {
      esRes <- elastic.indexOrgRole(orgRole)
    } yield {
      log.debug(s"updateOrgRole: ${esRes.toString}")
      List(
        updateOrgRoleStatement.bind()
          .setString("id", orgRole.id)
          .setString("name", orgRole.name)
          .setString("description",  orgRole.description.getOrElse(""))
          .setString("updated_at",  orgRole.updatedAt.toString)
          .setBool("active", orgRole.active)
      )
    }
  }

  private def deactivateOrgRole(event: OrgRoleDeactivated) = {
    for {
      esRes <- elastic.deactivateOrgRole(event.id)
    } yield {
      log.debug(s"deactivateOrgRole: ${esRes.toString}")
      List(
        deactivateOrgRoleStatement
          .bind()
          .setString("id", event.id)
      )
    }
  }

  private def activateOrgRole(event: OrgRoleActivated) = {
    for {
      esRes <- elastic.activateOrgRole(event.id)
    } yield {
      log.debug(s"activateOrgRole: ${esRes.toString}")
      List(
        activateOrgRoleStatement
          .bind()
          .setString("id", event.id)
      )
    }
  }


}
