package annette.authorization.impl

import akka.Done
import annette.authorization.api._
import com.datastax.driver.core.{PreparedStatement, Row}
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

private[impl] class UserRoleAssignmentRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  val initialized = init.recover { case th => th.printStackTrace(); throw th; }

  private var insertUserRoleAssignmentStatement: PreparedStatement = null
  private var insertRoleUserAssignmentStatement: PreparedStatement = null
  private var deleteUserRoleAssignmentStatement: PreparedStatement = null
  private var deleteRoleUserAssignmentStatement: PreparedStatement = null
  private var findRolesAssignedToUserStatement: PreparedStatement = null
  private var findUsersAssignedToRoleStatement: PreparedStatement = null

  private def init: Future[Done] = {
    println("preparation start...")
    for {
      _ <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS user_role_assignments (
          user_id text,
          role_id text,
          PRIMARY KEY (user_id, role_id)
        )
      """).recover { case th => th.printStackTrace(); throw th; }
      _ <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS role_user_assignments (
          role_id text,
          user_id text,
          PRIMARY KEY (role_id, user_id)
        )
      """).recover { case th => th.printStackTrace(); throw th; }
      insertUserRoleAssignment <- session.prepare("""
        INSERT INTO user_role_assignments(user_id, role_id) VALUES (:uid,  :rid)
      """)
      insertRoleUserAssignment <- session.prepare("""
        INSERT INTO role_user_assignments(role_id, user_id) VALUES (:rid,  :uid)
      """)
      deleteUserRoleAssignment <- session.prepare("""
         DELETE FROM user_role_assignments WHERE user_id = :uid and role_id = :rid
      """)
      deleteRoleUserAssignment <- session.prepare("""
        DELETE FROM role_user_assignments WHERE user_id = :uid and role_id = :rid
      """)

      findRolesAssignedToUser <- session.prepare("""
        select role_id from user_role_assignments where user_id = :uid
      """)
      findUsersAssignedToRole <- session.prepare("""
        select user_id from role_user_assignments where role_id = :rid
      """)
    } yield {
      insertUserRoleAssignmentStatement = insertUserRoleAssignment
      insertRoleUserAssignmentStatement = insertRoleUserAssignment
      deleteUserRoleAssignmentStatement = deleteUserRoleAssignment
      deleteRoleUserAssignmentStatement = deleteRoleUserAssignment
      findRolesAssignedToUserStatement = findRolesAssignedToUser
      findUsersAssignedToRoleStatement = findUsersAssignedToRole
      println("preparation done !!!!!")
      Done
    }
  }

  private def assignUserToRole(userId: UserId, roleId: RoleId): Future[Done] = {
    for {
      _ <- initialized
      stmt1 = insertUserRoleAssignmentStatement
        .bind()
        .setString("uid", userId)
        .setString("rid", roleId)
      _ <- session.executeWrite(stmt1)
      stmt2 = insertRoleUserAssignmentStatement
        .bind()
        .setString("uid", userId)
        .setString("rid", roleId)
      res2 <- session.executeWrite(stmt2)
    } yield res2
  }

  def assignUserToRoles(userId: UserId, roleIds: immutable.Set[RoleId]): Future[Done] = {
    Future
      .traverse(roleIds)(roleId => assignUserToRole(userId, roleId))
      .map(_ => Done)
  }

  def unassignUserFromRoles(userId: UserId, roleIds: immutable.Set[RoleId]): Future[Done] = {
    Future
      .traverse(roleIds)(roleId => unassignUserFromRole(userId, roleId))
      .map(_ => Done)
  }

  private def unassignUserFromRole(userId: UserId, roleId: RoleId): Future[Done] = {
    for {
      _ <- initialized
      stmt1 = deleteUserRoleAssignmentStatement
        .bind()
        .setString("uid", userId)
        .setString("rid", roleId)
      _ <- session.executeWrite(stmt1)
      stmt2 = deleteRoleUserAssignmentStatement
        .bind()
        .setString("uid", userId)
        .setString("rid", roleId)
      res2 <- session.executeWrite(stmt2)
    } yield res2
  }

  def findRolesAssignedToUser(userId: UserId): Future[immutable.Set[RoleId]] = {
    for {
      _ <- initialized
      stmt = findRolesAssignedToUserStatement
        .bind()
        .setString("uid", userId)
      res <- session.selectAll(stmt).map(_.map(_.getString("role_id")).toSet)
    } yield res
  }

  def findUsersAssignedToRole(roleId: RoleId): Future[immutable.Set[UserId]] = {
    for {
      _ <- initialized
      stmt = findUsersAssignedToRoleStatement
        .bind()
        .setString("rid", roleId)
      res <- session.selectAll(stmt).map(_.map(_.getString("user_id")).toSet)
    } yield res
  }
}
