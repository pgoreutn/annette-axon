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
import annette.authorization.api.model.Role
import annette.authorization.impl.assignment.UserRoleAssignmentRepository
import com.datastax.driver.core.PreparedStatement
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.sksamuel.elastic4s.ElasticClient
import org.slf4j.LoggerFactory
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

private[impl] class RoleEventProcessor(
    session: CassandraSession,
    readSide: CassandraReadSide,
    userRoleAssignmentRepository: UserRoleAssignmentRepository,
    elastic: RoleElastic,
    val configuration: Configuration,
)(implicit val ec: ExecutionContext)
    extends ReadSideProcessor[RoleEvent] {
  private val log = LoggerFactory.getLogger(classOf[RoleEventProcessor])

  private var insertRoleStatement: PreparedStatement = null
  private var updateRoleStatement: PreparedStatement = null
  private var deleteRoleStatement: PreparedStatement = null

  private var insertRolePermissionStatement: PreparedStatement = null
  private var deleteRolePermissionsStatement: PreparedStatement = null

  def buildHandler = {
    readSide
      .builder[RoleEvent]("roleEventOffset")
      .setGlobalPrepare(globalPrepare)
      .setPrepare(_ => prepareStatements())
      .setEventHandler[RoleCreated](e => insertRole(e.event.role))
      .setEventHandler[RoleUpdated](e => updateRole(e.event.role))
      .setEventHandler[RoleDeleted](e => deleteRole(e.event))
      .setEventHandler[RolePermissionDeleted](e => deleteRolePermission(e.event))
      .build
  }

  def aggregateTags = RoleEvent.Tag.allTags

  private def globalPrepare() = {
    for {
      esRes <- elastic.createRolesIndex
      _ <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS roles (
          id text PRIMARY KEY,
          name text,
          description text
        )
      """)
      _ <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS role_permissions (
          role_id text,
          permission_id text,
          arg1 text,
          arg2 text,
          arg3 text,
          PRIMARY KEY (role_id, permission_id, arg1, arg2, arg3)
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
      insertRole <- session.prepare("""
        INSERT INTO roles(id, name, description) VALUES (?, ?, ?)
      """)
      updateRole <- session.prepare("""
        UPDATE roles SET name = ?, description = ? WHERE id = ?
      """)
      deleteRole <- session.prepare("""
        DELETE FROM roles WHERE id = ?
      """)
      insertRolePermission <- session.prepare("""
        INSERT INTO role_permissions(role_id, permission_id, arg1, arg2, arg3) VALUES (?, ?, ?, ?, ?)
      """)
      deleteRolePermissions <- session.prepare("""
        DELETE FROM role_permissions WHERE role_id = ?
      """)
    } yield {
      insertRoleStatement = insertRole
      updateRoleStatement = updateRole
      deleteRoleStatement = deleteRole
      insertRolePermissionStatement = insertRolePermission
      deleteRolePermissionsStatement = deleteRolePermissions
      Done
    }
  }

  private def insertRole(role: Role) = {

    for {
      esRes <- elastic.indexRole(role)
    } yield {
      log.debug(s"insertRole: ${esRes.toString}")
      List(
        insertRoleStatement.bind(role.id, role.name, role.description.getOrElse(""))
      ) ++ role.permissions.map(p => insertRolePermissionStatement.bind(role.id, p.id, p.arg1, p.arg2, p.arg3))
    }
  }

  private def updateRole(role: Role) = {
    for {
      esRes <- elastic.indexRole(role)
    } yield {
      log.debug(s"updateRole: ${esRes.toString}")
      List(
        updateRoleStatement.bind(role.name, role.description.getOrElse(""), role.id)
      ) ++ role.permissions.map(p => insertRolePermissionStatement.bind(role.id, p.id, p.arg1, p.arg2, p.arg3))
    }
  }

  private def deleteRole(event: RoleDeleted) = {
    for {
      esRes <- elastic.deleteRole(event.id)
    } yield {
      log.debug(s"deleteRole: ${esRes.toString}")
      List(
        deleteRoleStatement.bind(event.id),
        deleteRolePermissionsStatement.bind(event.id)
      )
    }
  }

  private def deleteRolePermission(event: RolePermissionDeleted) = {
    log.debug(s"deleteRolePermission: ${event.id}")

    Future.successful(
      List(
        deleteRolePermissionsStatement.bind(event.id)
      )
    )
  }

}
