package annette.authorization.impl

import annette.authorization.api.{Permission, PermissionId, RoleId, RoleSummary}
import com.datastax.driver.core.Row
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConversions._

private case class Subrole(role: RoleId, subrole: RoleId)

private[impl] class RoleRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  private lazy val checkPermissionStatementFuture =
    session.prepare("""
        select count(*) as cnt
          from role_permissions
          where role_id in :roles and permission_id = :pid and arg1 = :a1 and arg2 = :a2 and arg3 = :a3
      """)
  private lazy val findPermissionStatementFuture = session.prepare("""
        select permission_id, arg1, arg2, arg3
          from role_permissions
          where role_id in :roles and permission_id = :pid
      """)
  private lazy val findSubrolesStatementFuture = session.prepare("""
        select role_id, subrole_id
          from role_subroles
          where role_id in :roles
      """)

  private def getAllRoles(roles: immutable.Set[RoleId]): Future[immutable.Set[RoleId]] = {
    for {
      findSubrolesStatement <- findSubrolesStatementFuture
      stmt = findSubrolesStatement
        .bind()
        .setList("roles", roles.toList)
      subroles <- session.selectAll(stmt).map(_.map(convertSubroles).toSet)
    } yield roles -- subroles.map(_.role) ++ subroles.map(_.subrole)
  }

  private def checkPermission(roles: immutable.Set[RoleId], permission: Permission) = {
    for {
      checkPermissionStatement <- checkPermissionStatementFuture
      stmt = checkPermissionStatement
        .bind()
        .setList("roles", roles.toList)
        .setString("pid", permission.id)
        .setString("a1", permission.arg1)
        .setString("a2", permission.arg2)
        .setString("a3", permission.arg3)
      res <- session.selectOne(stmt)
    } yield {
      // val r =
      res.exists(r => if (r.getLong("cnt") > 0) true else false)
      // println(s"checkPermission($roles, $permission): $res, $r")
      // r
    }
  }

  def checkAllPermissions(roles: immutable.Set[RoleId], permissions: immutable.Set[Permission]): Future[Boolean] = {
    Future
      .traverse(permissions)(permission => checkPermission(roles, permission))
      .map(set => set.forall(r => r))
  }

  def checkAnyPermissions(roles: immutable.Set[RoleId], permissions: immutable.Set[Permission]): Future[Boolean] = {
    Future
      .find(permissions.map(permission => checkPermission(roles, permission)))(r => r)
      .map(_.getOrElse(false))
  }

  def findPermissions(roles: immutable.Set[RoleId], permissionIds: immutable.Set[PermissionId]): Future[Set[Permission]] = {
    Future
      .foldLeft(
        permissionIds.map(permissionId => findPermission(roles, permissionId))
      )(immutable.Set.empty[Permission])(_ ++ _)
  }

  private def findPermission(roles: immutable.Set[RoleId], permissionId: PermissionId) = {
    for {
      findPermissionStatement <- findPermissionStatementFuture
      stmt = findPermissionStatement
        .bind()
        .setList("roles", roles.toList)
        .setString("pid", permissionId)
      res <- session.selectAll(stmt).map(_.map(convertPermission).toSet)
    } yield res
  }

  def findRoles(filter: String): Future[immutable.Set[RoleSummary]] = {
    val filterLC = filter.toLowerCase
    // Don't use in production
    for {
      seq <- selectRoles
    } yield {
      if (filter.isEmpty) {
        seq.to[collection.immutable.Set]
      } else {

        seq
          .filter { summary =>
            summary.id.toLowerCase.contains(filterLC) ||
              summary.name.toLowerCase.contains(filterLC) ||
              summary.description.getOrElse("").toLowerCase.contains(filterLC)
          }
          .to[collection.immutable.Set]
      }
    }
  }

  private def selectRoles = {
    session.selectAll("SELECT * FROM roles").map(_.map(convertRoleSummary))
  }

  private def convertRoleSummary(role: Row): RoleSummary = {
    RoleSummary(
      role.getString("id"),
      role.getString("name"), {
        val s = role.getString("description")
        if (s.nonEmpty) Some(s) else None
      },
      role.getBool("is_composite")
    )
  }

  private def convertPermission(role: Row): Permission = {
    Permission(
      role.getString("permission_id"),
      role.getString("arg1"),
      role.getString("arg2"),
      role.getString("arg3")
    )
  }

  private def convertSubroles(role: Row): Subrole = {
    Subrole(
      role.getString("role_id"),
      role.getString("sub_id")
    )
  }
}
