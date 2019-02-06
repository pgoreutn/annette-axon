package loader

import annette.authorization.api.AuthorizationService
import annette.security.auth.authentication.{AuthenticatedAction, KeycloackAuthenticator}
import annette.security.auth.authorization.{AuthorizedActionFactory, DefaultAuthorizer, DefaultRoleProvider}
import annette.security.user.UserService
import axon.bpm.repository.api.BpmRepositoryService
import axon.knowledge.repository.api.KnowledgeRepositoryService
import axon.rest.bpm.repository.BpmDiagramController
import axon.rest.knowledge.repository.DataSchemaController
import com.lightbend.lagom.scaladsl.api.{LagomConfigComponent, ServiceAcl, ServiceInfo}
import com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.rp.servicediscovery.lagom.scaladsl.LagomServiceLocatorComponents
import com.softwaremill.macwire._
import controllers.{AssetsComponents, HomeController}
import play.api.ApplicationLoader.Context
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.{BodyParsers, EssentialFilter}
import play.api.{ApplicationLoader, BuiltInComponentsFromContext, Mode}
import router.Routes

import scala.collection.immutable
import scala.concurrent.ExecutionContext

abstract class WebGateway(context: Context)
    extends BuiltInComponentsFromContext(context)
    with AssetsComponents
    //with HttpFiltersComponents
    with AhcWSComponents
    with LagomConfigComponent
    with LagomServiceClientComponents {

  def httpFilters: Seq[EssentialFilter] = Seq()

  override lazy val serviceInfo: ServiceInfo = ServiceInfo(
    "axon-backend",
    Map(
      "axon-backend" -> immutable.Seq(ServiceAcl.forPathRegex("(?!/api/).*"))
    )
  )
  implicit override lazy val executionContext: ExecutionContext = actorSystem.dispatcher

  override lazy val router = {
    val prefix = "/"
    wire[Routes]
  }

  lazy val bpmRepositoryService = serviceClient.implement[BpmRepositoryService]
  lazy val knowledgeRepositoryService = serviceClient.implement[KnowledgeRepositoryService]
  lazy val authzService = serviceClient.implement[AuthorizationService]

  lazy val userService = wire[UserService]

  lazy val authValidator = wire[KeycloackAuthenticator]
  lazy val auth = wire[AuthenticatedAction]
  lazy val roleProvider = wire[DefaultRoleProvider]
  lazy val authorizer = wire[DefaultAuthorizer]
  lazy val authz = wire[AuthorizedActionFactory]
  lazy val parser = wire[BodyParsers.Default]

  lazy val main = wire[HomeController]
  lazy val bpmDiagramController = wire[BpmDiagramController]
  lazy val dataSchemaController = wire[DataSchemaController]

}

class WebGatewayLoader extends ApplicationLoader {
  override def load(context: Context) = context.environment.mode match {
    case Mode.Dev =>
      (new WebGateway(context) with LagomDevModeComponents).application
    case _ =>
      (new WebGateway(context) with LagomServiceLocatorComponents).application
  }
}
