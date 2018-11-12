package annette.authorization.impl

import akka.Done
import annette.authorization.api._
import com.datastax.driver.core.Row
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConversions._

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
    } yield res.exists(r => if (r.getLong("cnt") > 0) true else false)
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

  private def convertRoleSummary(row: Row): RoleSummary = {
    RoleSummary(
      row.getString("id"),
      row.getString("name"), {
        val s = row.getString("description")
        if (s.nonEmpty) Some(s) else None
      }
    )
  }

  private def convertPermission(row: Row): Permission = {
    Permission(
      row.getString("permission_id"),
      row.getString("arg1"),
      row.getString("arg2"),
      row.getString("arg3")
    )
  }
}
