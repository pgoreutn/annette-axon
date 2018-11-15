package annette.security.auth.authentication

import java.util.UUID

import annette.security.auth.{SessionData, UserPrincipal}
import annette.shared.exceptions.AnnetteException
import pdi.jwt.exceptions.JwtExpirationException
import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.api.Configuration
import play.api.mvc.Request

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class KeycloackAuthenticator(configuration: Configuration) extends Authenticator {

  val clientId = configuration.get[String]("annette.security.client.clientId")

  val key = configuration.get[String]("annette.security.client.publicKey")
  val debugMode = configuration.getOptional[Boolean]("annette.security.client.debug").getOrElse(false)
  val publicKey = s"-----BEGIN PUBLIC KEY-----\n$key\n-----END PUBLIC KEY-----"

  override def authenticate[A](request: Request[A])(implicit ec: ExecutionContext): Future[SessionData] = {
    if (debugMode) fakeAuthenticate(request)
    else realAuthenticate(request)
  }

  def fakeAuthenticate[A](request: Request[A])(implicit ec: ExecutionContext): Future[SessionData] = {
    Future.successful(
      SessionData(
        UserPrincipal(
          userId = UUID.randomUUID().toString,
          username = "john.doe",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@acme.com",
          token = ""
        )
      )
    )
  }

  def realAuthenticate[A](request: Request[A])(implicit ec: ExecutionContext): Future[SessionData] = {
    Future {
      Try {
        val jwt = request.headers.get("Authorization").get.split(" ")(1)
        val json = JwtJson.decodeJson(jwt, publicKey, Seq(JwtAlgorithm.RS256)).get
        // println(Json.prettyPrint(json))
        val superUser = Try {
          val roles = (json \ "resource_access" \ clientId \ "roles").as[Seq[String]]
          roles.contains("iddqd")
        }.getOrElse(false)
        SessionData(
          UserPrincipal(
            userId = (json \ "sub").as[String],
            username = (json \ "preferred_username").as[String],
            firstName = (json \ "given_name").as[String],
            lastName = (json \ "family_name").as[String],
            email = (json \ "email").as[String],
            token = jwt,
            superUser = superUser
          )
        )
      } match {
        case Success(sd)                   => sd
        case Failure(ex: AnnetteException) =>
          //ex.printStackTrace()
          throw ex
        case Failure(ex: JwtExpirationException) =>
          ex.printStackTrace()
          throw new SessionTimeoutException()
        case Failure(th: Throwable) =>
          //th.printStackTrace()
          throw new AuthenticationFailedException()
      }
    }

  }

}
