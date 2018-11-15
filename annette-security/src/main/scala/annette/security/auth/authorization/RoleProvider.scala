package annette.security.auth.authorization
import annette.authorization.api.RoleId
import annette.security.auth.UserId

import scala.collection._
import scala.concurrent.Future;

trait RoleProvider {
  def get(userId: UserId): Future[immutable.Set[RoleId]]
}
