package controllers
import annette.authorization.api.Permission
import annette.security.authentication.AuthenticatedAction
import annette.security.authorization._
import axon.bpm.repository.api.BpmRepositoryService
import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.ws._

import scala.concurrent.ExecutionContext
import scala.io.Source

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(
                               assets: Assets,
                               authenticated: AuthenticatedAction,
                               authorized: AuthorizedActionFactory,
                               cc: ControllerComponents,
                               implicit val ec: ExecutionContext)
    extends AbstractController(cc) {

  def index(file: String = "") = assets.versioned("/public/dist/", "index.html")

  def keycloak = Action { request: Request[AnyContent] =>
    val config = Source.fromResource("keycloak.json").mkString
    Ok(config)
  }

  def heartbeat() = authenticated { implicit request =>
    Ok("ok")
  }

  def auth() =
     authorized(
       CheckAny(
         Permission("axon.bpm.executeAll"),
         Permission("axon.bpm.execute", "process1"),
         Permission("axon.bpm.execute", "process2")
       ) )
    //authorized(AuthorizationQuery())
    { implicit request =>
   // request.sessionData.
      Ok("ok")
    }
}
