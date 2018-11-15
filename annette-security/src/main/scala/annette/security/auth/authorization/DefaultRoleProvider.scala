package annette.security.auth.authorization
import annette.authorization.api.{AuthorizationService, RoleId}
import annette.security.auth.UserId
import javax.inject._

import scala.concurrent.Future

@Singleton
class DefaultRoleProvider @Inject()(authorizationService: AuthorizationService) extends RoleProvider {
  override def get(userId: UserId): Future[Set[RoleId]] = {
    authorizationService.findRolesAssignedToUser(userId).invoke()
  }
}
