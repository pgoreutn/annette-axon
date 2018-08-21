package controllers
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
class HomeController @Inject()(ws: WSClient,
                               assets: Assets,
                               bpmService: BpmRepositoryService,
                               cc: ControllerComponents,
                               implicit val ec: ExecutionContext)
    extends AbstractController(cc) {

  final val KEYCLOAK_SERVER = "http://localhost:8180"
  final val REALM = "Annette"


  def index(file: String = "") = assets.versioned("/public/dist/", "index.html")

  def token = Action.async { request: Request[AnyContent] =>
    val jwt = request.headers.get("Authorization").get.split(" ")(1);

    val future = ws
      .url(
        s"$KEYCLOAK_SERVER/auth/realms/$REALM/protocol/openid-connect/userinfo"
      )
      .addHttpHeaders("Accept" -> "application/json")
      .addHttpHeaders("Authorization" -> s"Bearer $jwt")
      .get()

    future.map { r =>
      println(r)
      Ok(r.json)
    }
  }


  def config = Action.async { request: Request[AnyContent] =>
    val jwt = request.headers.get("Authorization").get.split(" ")(1);

    val future = ws
      .url(
        s"$KEYCLOAK_SERVER/auth/realms/$REALM/.well-known/openid-configuration"
      )
      .addHttpHeaders("Accept" -> "application/json")
      // .addHttpHeaders("Authorization" -> s"Bearer $jwt")
      .get()

    future.map { r =>
      println(r)
      Ok(r.json)
    }
  }


  def keycloak = Action { request: Request[AnyContent] =>
    val config = Source.fromResource("keycloak.json").mkString
    Ok(config)
  }

}
