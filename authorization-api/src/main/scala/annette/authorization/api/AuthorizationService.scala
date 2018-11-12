package annette.authorization.api;

import akka.{Done, NotUsed}
import annette.shared.exceptions.AnnetteExceptionSerializer
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import scala.collection._

trait AuthorizationService extends Service {

  def createRole: ServiceCall[BaseRole, BaseRole]
  def updateRole: ServiceCall[BaseRole, BaseRole]
  def deleteRole(id: RoleId): ServiceCall[NotUsed, Done]
  def findRoleById(id: RoleId): ServiceCall[NotUsed, BaseRole]
  def findRoles: ServiceCall[String, immutable.Set[RoleSummary]]

  def checkAllPermissions: ServiceCall[CheckPermissions, Boolean]
  def checkAnyPermissions: ServiceCall[CheckPermissions, Boolean]
  def findPermissions: ServiceCall[FindPermissions, immutable.Set[Permission]]

  final override def descriptor = {
    import Service._
    // @formatter:off
    named("authorization")
      .withCalls(
        restCall(Method.POST, "/api/authorization/role", createRole),
        restCall(Method.PUT, "/api/authorization/role", updateRole),
        restCall(Method.DELETE, "/api/authorization/role/:id", deleteRole _),
        restCall(Method.GET, "/api/authorization/role/:id", findRoleById _),
        restCall(Method.POST, "/api/authorization/findRoles", findRoles _),
        restCall(Method.POST, "/api/authorization/checkAll", checkAllPermissions _),
        restCall(Method.POST, "/api/authorization/checkAny", checkAnyPermissions _),
        restCall(Method.POST, "/api/authorization/find", findPermissions _),

      )
      .withExceptionSerializer(new AnnetteExceptionSerializer())
      .withAutoAcl(true)
    // @formatter:on
  }
}
