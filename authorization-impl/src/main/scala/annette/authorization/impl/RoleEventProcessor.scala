package annette.authorization.impl

import akka.Done
import annette.authorization.api.Role
import com.datastax.driver.core.PreparedStatement
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}

import scala.concurrent.{ExecutionContext, Future}

private[impl] class RoleEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext)
    extends ReadSideProcessor[RoleEvent] {
  private var insertRoleStatement: PreparedStatement = null
  private var updateRoleStatement: PreparedStatement = null
  private var deleteRoleStatement: PreparedStatement = null

  private var insertRolePermissionStatement: PreparedStatement = null
  private var deleteRolePermissionsStatement: PreparedStatement = null

  def buildHandler = {
    readSide
      .builder[RoleEvent]("roleEventOffset")
      .setGlobalPrepare(createTables)
      .setPrepare(_ => prepareStatements())
      .setEventHandler[RoleCreated](e => insertRole(e.event.role))
      .setEventHandler[RoleUpdated](e => updateRole(e.event.role))
      .setEventHandler[RoleDeleted](e => deleteRole(e.event))
      .setEventHandler[RolePermissionDeleted](e => deleteRolePermission(e.event))
      .build
  }

  def aggregateTags = RoleEvent.Tag.allTags

  private def createTables() = {
    for {
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

    } yield Done
  }

  private def prepareStatements() = {
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
    println(s"insertRole: ${role.id}")

    Future.successful(
      List(
        insertRoleStatement.bind(role.id, role.name, role.description.getOrElse(""))
      ) ++ role.permissions.map(p => insertRolePermissionStatement.bind(role.id, p.id, p.arg1, p.arg2, p.arg3))
    )
  }

  private def updateRole(role: Role) = {
    println(s"updateRole: ${role.id}")

    Future.successful(
      List(
        updateRoleStatement.bind(role.name, role.description.getOrElse(""), role.id)
      ) ++ role.permissions.map(p => insertRolePermissionStatement.bind(role.id, p.id, p.arg1, p.arg2, p.arg3))
    )
  }

  private def deleteRole(event: RoleDeleted) = {
    println(s"deleteRole: ${event.id}")

    Future.successful(
      List(
        deleteRoleStatement.bind(event.id),
        deleteRolePermissionsStatement.bind(event.id),
      ))
  }

  private def deleteRolePermission(event: RolePermissionDeleted) = {
    println(s"deleteRole: ${event.id}")

    Future.successful(
      List(
        deleteRolePermissionsStatement.bind(event.id),
      ))
  }

}
