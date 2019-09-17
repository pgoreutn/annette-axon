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

import annette.authorization.api.model._
import com.datastax.driver.core.Row
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.sksamuel.elastic4s.ElasticClient
import play.api.Configuration

import scala.collection.JavaConverters._
import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

private[impl] class RoleRepository(session: CassandraSession, val configuration: Configuration, val elasticClient: ElasticClient)(
  implicit val ec: ExecutionContext
) {

  private def checkPermission(roles: immutable.Set[RoleId], permission: Permission): Future[Boolean] = {
    for {
      res <- session.selectOne(
        """
          |select count(*) as cnt
          |   from role_permissions
          |   where role_id in ? and permission_id = ? and arg1 = ? and arg2 = ? and arg3 = ?
          |   """.stripMargin,
        roles.toList.asJava,
        permission.id,
        permission.arg1,
        permission.arg2,
        permission.arg3
      )
    } yield res.exists(r => r.getLong("cnt") > 0 )
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

  private def findPermission(roles: immutable.Set[RoleId], permissionId: PermissionId): Future[Set[Permission]] = {
    for {
      res <- session
        .selectAll(
          """
            |select permission_id, arg1, arg2, arg3
            |  from role_permissions
            |  where role_id in ? and permission_id = ?
            |""".stripMargin,
          roles.toList.asJava,
          permissionId
        )
        .map(_.map(convertPermission).toSet)
    } yield res
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
