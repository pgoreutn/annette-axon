package axon.rest.bpm.repository
import annette.security.auth.authentication.AuthenticatedAction
import annette.security.auth.authorization.{AuthorizedActionFactory, CheckAny}
import annette.shared.exceptions.AnnetteException
import axon.bpm.engine.api.BpmEngineService
import axon.bpm.repository.api.model.{BpmDiagram, BpmDiagramSummary}
import axon.bpm.repository.api.BpmRepositoryService
import axon.rest.bpm.permission.BpmPermissions._
import javax.inject.Inject
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

class BpmDiagramController @Inject()(
    authorized: AuthorizedActionFactory,
    bpmRepositoryService: BpmRepositoryService,
    bpmEngineService: BpmEngineService,
    cc: ControllerComponents,
    implicit val ec: ExecutionContext)
    extends AbstractController(cc) {

  implicit val findBpmDiagramsFormat = Json.format[FindBpmDiagrams]
  //implicit val bpmDiagramSummaryFormat = Json.format[BpmDiagramSummary]
  //implicit val bpmDiagramFormat = Json.format[BpmDiagram]

  def find() = authorized(CheckAny(BPM_DIAGRAM_VIEW)).async(parse.json[FindBpmDiagrams]) { implicit request =>
    val findBpmDiagrams = request.body
    bpmRepositoryService.findBpmDiagrams
      .invoke(findBpmDiagrams.filter)
      .map(r => Ok(Json.toJson(r)))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }
  def findById(id: String) = authorized(CheckAny(BPM_DIAGRAM_VIEW)).async { implicit request =>
    bpmRepositoryService
      .findBpmDiagramById(id)
      .invoke()
      .map(r => Ok(Json.toJson(r)))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }

  def create = authorized(CheckAny(BPM_REPOSITORY_CONTROL)).async(parse.json[BpmDiagram]) { implicit request =>
    val bpmDiagram = request.body
    bpmRepositoryService.createBpmDiagram
      .invoke(bpmDiagram)
      .map(r => Ok(Json.toJson(r)))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }
  def update() = authorized(CheckAny(BPM_REPOSITORY_CONTROL)).async(parse.json[BpmDiagram]) { implicit request =>
    val bpmDiagram = request.body
    bpmRepositoryService.updateBpmDiagram
      .invoke(bpmDiagram)
      .map(r => Ok(Json.toJson(r)))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }
  def delete(id: String) = authorized(CheckAny(BPM_REPOSITORY_CONTROL)).async { implicit request =>
    bpmRepositoryService
      .deleteBpmDiagram(id)
      .invoke()
      .map(_ => Ok(Json.toJson(Map("deleted" -> "true"))))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }

  def deploy(id: String) = authorized(CheckAny(BPM_REPOSITORY_CONTROL)).async { implicit request =>
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

case class FindBpmDiagrams(filter: String)
