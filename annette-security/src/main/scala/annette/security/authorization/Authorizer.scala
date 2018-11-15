package annette.security.authorization

import annette.authorization.api.RoleId
import annette.security.SessionData
import play.api.mvc.Request

import scala.collection._
import scala.concurrent.{ExecutionContext, Future}

trait Authorizer {
  def authorize[A](
      request: Request[A],
      sessionData: SessionData,
      roles: immutable.Set[RoleId],
      authorizationQuery: AuthorizationQuery
  )(implicit ec: ExecutionContext): Future[SessionData]
}
