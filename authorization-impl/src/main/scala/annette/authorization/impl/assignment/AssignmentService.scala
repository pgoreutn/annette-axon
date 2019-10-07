package annette.authorization.impl.assignment

import akka.Done
import akka.actor.ActorSystem
import akka.stream.Materializer
import annette.authorization.api.model.{AuthorizationPrincipal, PrincipalAssignment, PrincipalId, RoleId}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

class AssignmentService(
    registry: PersistentEntityRegistry,
    system: ActorSystem,
    assignmentRepository: AssignmentRepository
)(implicit ec: ExecutionContext, mat: Materializer) {

  private def refFor(id: String) = registry.refFor[AssignmentEntity](id)

  def assignPrincipal(assignment: PrincipalAssignment): Future[Done] = {
    refFor(assignment.id).ask(AssignPrincipal(assignment))
  }

  def unassignPrincipal(assignment: PrincipalAssignment): Future[Done] = {
    refFor(assignment.id).ask(UnassignPrincipal(assignment))
  }

  def findPrincipalsAssignedToRole(roleId: RoleId): Future[immutable.Set[AuthorizationPrincipal]] = {
    assignmentRepository.findPrincipalsAssignedToRole(roleId)
  }

  def findRolesAssignedToPrincipal(principal: AuthorizationPrincipal): Future[immutable.Set[RoleId]] = {
    assignmentRepository.findRolesAssignedToPrincipal(principal)
  }

  def findRolesAssignedToPrincipals(principals: immutable.Set[AuthorizationPrincipal]): Future[immutable.Set[RoleId]] = {
    Future.foldLeft(
      principals.map(principal => assignmentRepository.findRolesAssignedToPrincipal(principal))
    )(immutable.Set.empty[RoleId])((acc, res) => acc ++ res)
  }

}
