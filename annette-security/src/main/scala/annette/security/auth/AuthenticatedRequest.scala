package annette.security.auth
import play.api.mvc.{Request, WrappedRequest};

case class AuthenticatedRequest[A](sessionData: SessionData, request: Request[A]) extends WrappedRequest[A](request)
