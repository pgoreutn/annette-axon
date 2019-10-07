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

package annette.authorization.api;

import akka.{Done, NotUsed}
import annette.authorization.api.model.{AuthorizationPrincipal, CheckPermissions, FindPermissions, Permission, PrincipalAssignment, Role, RoleFilter, RoleFindResult, RoleId}
import annette.shared.exceptions.AnnetteExceptionSerializer
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

import scala.collection._

trait AuthorizationService extends Service {

  def createRole: ServiceCall[Role, Role]
  def updateRole: ServiceCall[Role, Role]
  def deleteRole(id: RoleId): ServiceCall[NotUsed, Done]
  def findRoleById(id: RoleId): ServiceCall[NotUsed, Role]
  def findRoles: ServiceCall[RoleFilter, RoleFindResult]

  def checkAllPermissions: ServiceCall[CheckPermissions, Boolean]
  def checkAnyPermissions: ServiceCall[CheckPermissions, Boolean]
  def findPermissions: ServiceCall[FindPermissions, immutable.Set[Permission]]

  def assignPrincipal: ServiceCall[PrincipalAssignment, Done]
  def unassignPrincipal: ServiceCall[PrincipalAssignment, Done]
  def findRolesAssignedToPrincipal: ServiceCall[AuthorizationPrincipal, immutable.Set[RoleId]]
  def findPrincipalsAssignedToRole(roleId: RoleId): ServiceCall[NotUsed, immutable.Set[AuthorizationPrincipal]]



  final override def descriptor = {
    import Service._
    // @formatter:off
    named("authorization")
      .withCalls(
        restCall(Method.POST, "/api/authorization/role", createRole),
        restCall(Method.PUT, "/api/authorization/role", updateRole),
        restCall(Method.DELETE, "/api/authorization/role/:roleId", deleteRole _),
        restCall(Method.GET, "/api/authorization/role/:roleId", findRoleById _),
        restCall(Method.POST, "/api/authorization/roles/find", findRoles _),

        restCall(Method.POST, "/api/authorization/permissions/checkAll", checkAllPermissions),
        restCall(Method.POST, "/api/authorization/permissions/checkAny", checkAnyPermissions),
        restCall(Method.POST, "/api/authorization/permissions/find", findPermissions),

        restCall(Method.POST, "/api/authorization/principal/assign", assignPrincipal _),
        restCall(Method.DELETE, "/api/authorization/principal/assign", unassignPrincipal _),
        restCall(Method.GET, "/api/authorization/principal/findRoles", findRolesAssignedToPrincipal _),
        restCall(Method.GET, "/api/authorization/principal/findPrincipals/:roleId", findPrincipalsAssignedToRole _),

      )
      .withExceptionSerializer(new AnnetteExceptionSerializer())
      .withAutoAcl(true)
    // @formatter:on
  }
}
