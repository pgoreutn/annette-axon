package annette.authorization.impl

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.{Done, NotUsed}
import annette.authorization.api._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.collection._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Implementation of the BpmService.
  */
class AuthorizationServiceImpl(registry: PersistentEntityRegistry, system: ActorSystem, roleRepository: RoleRepository)(
    implicit ec: ExecutionContext,
    mat: Materializer)
    extends AuthorizationService {

  private def refFor(id: RoleId) = registry.refFor[RoleEntity](id)

  override def createRole: ServiceCall[Role, Role] = ServiceCall { role =>
    refFor(role.id)
      .ask(CreateRole(role))
  }
  override def updateRole: ServiceCall[Role, Role] = ServiceCall { role =>
    refFor(role.id)
      .ask(UpdateRole(role))
  }
  override def deleteRole(id: RoleId): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    refFor(id)
      .ask(DeleteRole(id))
  }

  override def findRoleById(id: RoleId): ServiceCall[NotUsed, Role] = ServiceCall { _ =>
    refFor(id).ask(FindRoleById(id)).map {
      case Some(role) => role
      case None       => throw RoleNotFound(id)
    }
  }
  override def findRoles: ServiceCall[String, immutable.Set[RoleSummary]] = ServiceCall { filter =>
    // TODO: temporary solution, should be implemented using ElasticSearch
    roleRepository.findRoles(filter.trim)
  }

  override def checkAllPermissions: ServiceCall[CheckPermissions, Boolean] = ServiceCall {
    case CheckPermissions(roles, permissions) =>
      if (roles.isEmpty || permissions.isEmpty) {
        Future.successful(false)
      } else {
        roleRepository.checkAllPermissions(roles, permissions)
      }

  }
  override def checkAnyPermissions: ServiceCall[CheckPermissions, Boolean] = ServiceCall {
    case CheckPermissions(roles, permissions) =>
      if (roles.isEmpty || permissions.isEmpty) {
        Future.successful(false)
      } else {
        roleRepository.checkAnyPermissions(roles, permissions)
      }
  }
  override def findPermissions: ServiceCall[FindPermissions, immutable.Set[Permission]] = ServiceCall {
    case FindPermissions(roles, permissionIds) =>
      if (roles.isEmpty || permissionIds.isEmpty) {
        Future.successful(immutable.Set.empty)
      } else {
        roleRepository.findPermissions(roles, permissionIds)
      }
  }
}
