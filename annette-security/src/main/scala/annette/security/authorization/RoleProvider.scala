package annette.security.authorization

import annette.authorization.api.RoleId
import annette.security.UserId

import scala.collection._
import scala.concurrent.Future

trait RoleProvider {
  def get(userId: UserId): Future[immutable.Set[RoleId]]
}
