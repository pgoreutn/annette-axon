package axon.rest.bpm.repository

import annette.security.auth.authentication.AuthenticatedAction
import annette.security.auth.authorization.{AuthorizedActionFactory, CheckAny}
import annette.shared.exceptions.AnnetteException
import axon.bpm.engine.api.{BpmEngineService, ProcessDef}
import axon.bpm.repository.api.model._
import axon.bpm.repository.api.BpmRepositoryService
import axon.knowledge.repository.api.KnowledgeRepositoryService
import axon.knowledge.repository.api.model.DataSchemaKey
import axon.rest.bpm.permission.BpmPermissions._
import javax.inject.Inject
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.collection._
import scala.concurrent.{ExecutionContext, Future}

class BusinessProcessController @Inject()(
    authorized: AuthorizedActionFactory,
    bpmRepositoryService: BpmRepositoryService,
    bpmEngineService: BpmEngineService,
    knowledgeRepositoryService: KnowledgeRepositoryService,
    cc: ControllerComponents,
    implicit val ec: ExecutionContext)
    extends AbstractController(cc) {

  implicit val findBusinessProcesssFormat = Json.format[FindBusinessProcesss]
  implicit val processDefDataFormat = Json.format[ProcessReferenceDetail]
  implicit val dataSchemaDataFormat = Json.format[DataSchemaDetail]
  implicit val businessProcessSummaryDtoFormat = Json.format[BusinessProcessSummaryDto]

  def find() = authorized(CheckAny(BUSINESS_PROCESS_VIEW)).async(parse.json[FindBusinessProcesss]) { implicit request =>
    val filter = request.body.filter
    (for {
      businessProcesses <- bpmRepositoryService.findBusinessProcess.invoke(filter)
      processDefsByKey <- findprocessDefsByKey(businessProcesses)
      processDefsById <- findProcessDefsById(businessProcesses)
      dataSchemas <- findDataSchemas(businessProcesses)
    } yield {
      val dto = businessProcesses.map { bp =>
      println("bp.processReference")
      println(bp.processReference)
        val processReferenceDetail = bp.processReference match {
          case ProcessReferenceByKey(key) =>
            processDefsByKey.get(key).map(pd => ProcessReferenceDetail(key, "Latest", pd.name))
          case ProcessReferenceById(id) =>
            processDefsById.get(id).map(pd => ProcessReferenceDetail(pd.key, pd.version.toString, pd.name))
        }
        val dataSchemaDetail = dataSchemas.get(bp.dataSchemaKey).map(ds => DataSchemaDetail(ds.key, ds.name))
        BusinessProcessSummaryDto(
          id = bp.id,
          name = bp.name,
          description = bp.description,
          processReference = bp.processReference,
          processReferenceDetail = processReferenceDetail,
          dataSchemaKey = bp.dataSchemaKey,
          dataSchemaDetail = dataSchemaDetail
        )
      }
      Ok(Json.toJson(dto))
    }).recover {
      case ex: AnnetteException =>
        BadRequest(ex.toMessage)
    }
  }

  private def findDataSchemas(businessProcesses: immutable.Seq[BusinessProcessSummary]) = {
    knowledgeRepositoryService.findDataSchemaByKeys
      .invoke(businessProcesses.map(_.dataSchemaKey))
      .map(_.map(ds => ds.key -> ds).toMap)
  }

  private def findProcessDefsById(businessProcesses: immutable.Seq[BusinessProcessSummary]): Future[Map[String, ProcessDef]] = {

    val ids = businessProcesses.map(_.processReference).flatMap {
      case ProcessReferenceByKey(_) => None
      case ProcessReferenceById(id) => Some(id)
    }
    if (ids.nonEmpty) {
      bpmEngineService.findProcessDefByIds.invoke(ids).map(_.map(pd => pd.key -> pd).toMap)
    } else {
      Future.successful(Map.empty)
    }

  }

  private def findprocessDefsByKey(businessProcesses: immutable.Seq[BusinessProcessSummary]): Future[Map[String, ProcessDef]] = {

    val keys = businessProcesses
      .map(_.processReference)
      .flatMap {
        case ProcessReferenceByKey(key) => Some(key)
        case ProcessReferenceById(_)    => None
      }
    if (keys.nonEmpty) {
      bpmEngineService.findProcessDefByKeys.invoke(keys).map(_.map(pd => pd.key -> pd).toMap)
    } else {
      Future.successful(Map.empty)
    }

  }

  def findById(id: String) = authorized(CheckAny(BUSINESS_PROCESS_VIEW)).async { implicit request =>
    bpmRepositoryService
      .findBusinessProcessById(id)
      .invoke()
      .map(r => Ok(Json.toJson(r)))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }

  def create = authorized(CheckAny(BPM_REPOSITORY_CONTROL)).async(parse.json[BusinessProcess]) { implicit request =>
    val businessProcess = request.body
    bpmRepositoryService.createBusinessProcess
      .invoke(businessProcess)
      .map(r => Ok(Json.toJson(r)))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }
  def update() = authorized(CheckAny(BPM_REPOSITORY_CONTROL)).async(parse.json[BusinessProcess]) { implicit request =>
    val businessProcess = request.body
    bpmRepositoryService.updateBusinessProcess
      .invoke(businessProcess)
      .map(r => Ok(Json.toJson(r)))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }
  def delete(id: String) = authorized(CheckAny(BPM_REPOSITORY_CONTROL)).async { implicit request =>
    bpmRepositoryService
      .deleteBusinessProcess(id)
      .invoke()
      .map(_ => Ok(Json.toJson(Map("deleted" -> "true"))))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }

}

case class FindBusinessProcesss(filter: String)

case class BusinessProcessSummaryDto(
    id: BusinessProcessId,
    name: String,
    description: Option[String],
    processReference: ProcessReference,
    processReferenceDetail: Option[ProcessReferenceDetail],
    dataSchemaKey: DataSchemaKey,
    dataSchemaDetail: Option[DataSchemaDetail],
)

case class ProcessReferenceDetail(
    key: String,
    version: String,
    name: String,
)

case class DataSchemaDetail(
    key: String,
    name: String,
)
