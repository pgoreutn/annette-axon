package annette.authorization.api;

import akka.{Done, NotUsed}
import annette.shared.exceptions.AnnetteExceptionSerializer
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import scala.collection._

trait AuthorizationService extends Service {

  def createRole: ServiceCall[Role, Role]
  def updateRole: ServiceCall[Role, Role]
  def deleteRole(id: RoleId): ServiceCall[NotUsed, Done]
  def findRoleById(id: RoleId): ServiceCall[NotUsed, Role]
  def findRoles: ServiceCall[String, immutable.Set[RoleSummary]]

  def checkAllPermissions: ServiceCall[CheckPermissions, Boolean]
  def checkAnyPermissions: ServiceCall[CheckPermissions, Boolean]
  def findPermissions: ServiceCall[FindPermissions, immutable.Set[Permission]]

  def assignUserToRoles(userId: UserId): ServiceCall[immutable.Set[RoleId], Done]
  def unassignUserFromRoles(userId: UserId): ServiceCall[immutable.Set[RoleId], Done]
  def findRolesAssignedToUser(userId: UserId): ServiceCall[NotUsed, immutable.Set[RoleId]]
  def findUsersAssignedToRole(roleId: RoleId): ServiceCall[NotUsed, immutable.Set[UserId]]

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

        restCall(Method.POST, "/api/authorization/user/assign/:userId", assignUserToRoles _),
        restCall(Method.POST, "/api/authorization/user/unassign/:userId", unassignUserFromRoles _),
        restCall(Method.GET, "/api/authorization/user/findRoles/:userId", findRolesAssignedToUser _),
        restCall(Method.GET, "/api/authorization/user/findUsers/:roleIdId", findUsersAssignedToRole _),

      )
      .withExceptionSerializer(new AnnetteExceptionSerializer())
      .withAutoAcl(true)
    // @formatter:on
  }
}
