package axon.rest.bpm.repository

import annette.security.auth.authentication.AuthenticatedAction
import annette.security.auth.authorization.{AuthorizedActionFactory, CheckAny}
import annette.shared.exceptions.AnnetteException
import axon.bpm.engine.api.{BpmEngineService, FindDecisionDefOptions, FindProcessDefOptions}
import axon.bpm.repository.api.BpmRepositoryService
import axon.rest.bpm.permission.BpmPermissions._
import javax.inject.Inject
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

class BpmDeploymentController @Inject()(
    authenticated: AuthenticatedAction,
    authorized: AuthorizedActionFactory,
    bpmRepositoryService: BpmRepositoryService,
    bpmEngineService: BpmEngineService,
    cc: ControllerComponents,
    implicit val ec: ExecutionContext)
    extends AbstractController(cc) {

  def find() = authorized(CheckAny(VIEW_BPM_DIAGRAM)).async(parse.json[FindProcessDefOptions]) { implicit request =>
    val filter = request.body
    bpmEngineService.findProcessDef
      .invoke(filter)
      .map(r => Ok(Json.toJson(r)))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }

  def deploy(id: String) = authorized(CheckAny(DEPLOY_BPM_DIAGRAM)).async { implicit request =>
    (for {
      bpmDiagram <- bpmRepositoryService
        .findBpmDiagramById(id)
        .invoke()
      deployment <- bpmEngineService.deploy.invoke(bpmDiagram)
    } yield {
      Ok(Json.toJson(deployment))
    }).recover {
      case ex: AnnetteException =>
        BadRequest(ex.toMessage)
    }
  }
}
