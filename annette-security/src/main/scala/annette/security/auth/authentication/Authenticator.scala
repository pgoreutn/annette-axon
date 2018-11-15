package annette.security.auth.authentication

import annette.security.auth.SessionData
import play.api.mvc.Request

import scala.concurrent.{ExecutionContext, Future}

trait Authenticator {
  def authenticate[A](request: Request[A])(implicit ec: ExecutionContext): Future[SessionData]
}
