package annette.security.authentication

import annette.security.SessionData
import play.api.mvc.Request

import scala.concurrent.{ExecutionContext, Future}

trait Authenticator {
  def authenticate[A](request: Request[A])(implicit ec: ExecutionContext): Future[SessionData]
}
