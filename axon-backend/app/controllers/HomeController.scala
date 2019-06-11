package controllers
import annette.authorization.api.Permission
import annette.security.auth.authentication.AuthenticatedAction
import annette.security.auth.authorization.{AuthorizedActionFactory, CheckAny}
import annette.security.user.{UserQuery, UserService}
import javax.inject._
import play.api.mvc._

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
    userService: UserService,
    cc: ControllerComponents,
    implicit val ec: ExecutionContext)
    extends AbstractController(cc) {

  def index = assets.versioned("/public/dist/", "index.html")

  def assetOrDefault(resource: String): Action[AnyContent] = {
    if (resource.contains(".")) assets.versioned("/public/dist/", resource) else index
  }

  def keycloak = Action { request: Request[AnyContent] =>
    val config = Source.fromResource("keycloak.json").mkString
    Ok(config)
  }

  def heartbeat() = authenticated { implicit request =>
    Ok("ok")
  }

  def user() = authenticated.async { implicit request =>
    userService
      //.findUsers(request.sessionData.principal.token, UserQuery())
        .findUserByIds(request.sessionData.principal.token, Set("a211feb8-3be1-412a-b5c3-be27bf6d872a", "a211feb8-3be1-412a-b5c3-be27bf6d872a1", "adb1c2be-f521-4f1f-bb52-fe9380bef05e"))
      .map { res => Ok(res.toString())}
  }
}
