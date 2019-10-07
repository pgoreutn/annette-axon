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

package annette.authorization.impl.assignment

import akka.Done
import annette.authorization.api.model._
import com.datastax.driver.core.PreparedStatement
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

private[impl] class AssignmentRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  def findRolesAssignedToPrincipal(principal: AuthorizationPrincipal): Future[immutable.Set[RoleId]] = {
    for {
      findRolesAssignedToPrincipalStatement <- session.prepare(
        """
        SELECT role_id FROM principal_to_role where principal_type = :principal_type and principal_id = :principal_id
      """
      )
      statement = findRolesAssignedToPrincipalStatement
        .bind()
        .setString("principal_type", principal.principalType)
        .setString("principal_id", principal.principalId)
      res <- session.selectAll(statement).map(_.map(_.getString("role_id")).toSet)
    } yield res
  }

  def findPrincipalsAssignedToRole(roleId: RoleId): Future[immutable.Set[AuthorizationPrincipal]] = {
    for {
      findPrincipalsAssignedToRoleStatement <- session.prepare("""
        SELECT principal_type, principal_id FROM role_to_principal where role_id = : role_id
      """)
      statement = findPrincipalsAssignedToRoleStatement
        .bind()
        .setString("role_id", roleId)
      res <- session
        .selectAll(statement)
        .map(
          _.map(
            row =>
              AuthorizationPrincipal(
                principalType = row.getString("principal_type"),
                principalId = row.getString("principal_id")
              )
          ).toSet
        )
    } yield res
  }
}
