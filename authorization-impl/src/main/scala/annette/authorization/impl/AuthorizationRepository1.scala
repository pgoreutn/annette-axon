package annette.authorization.impl

import akka.Done
import com.datastax.driver.core.PreparedStatement
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}

import scala.concurrent.{ExecutionContext, Future}

private[impl] class AuthorizationRepository1(session: CassandraSession)(implicit ec: ExecutionContext) {
  private var insertRoleStatement: PreparedStatement = null
  private var updateRoleStatement: PreparedStatement = null
  private var deleteRoleStatement: PreparedStatement = null

  private var insertRolePermissionStatement: PreparedStatement = null
  private var deleteRolePermissionsStatement: PreparedStatement = null

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
          role_id text ,
          permission_id text ,
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

  /*private def insertSchema(event: SchemaCreated) = {
    println(s"insertSchema: ${event.id}")
    Future.successful(
      List(
        insertSchemaStatement.bind(event.id, event.name, event.description.getOrElse(""), event.notation)
      ))
  }

  private def updateSchema(event: SchemaUpdated) = {
    println(s"updateSchema: ${event.id}")

    Future.successful(
      List(
        updateSchemaStatement.bind(event.name, event.description.getOrElse(""), event.id)
      ))
  }

  private def deleteSchema(event: SchemaDeleted) = {
    println(s"deleteSchema: ${event.id}")

    Future.successful(
      List(
        deleteSchemaStatement.bind(event.id)
      ))
  }*/

}
