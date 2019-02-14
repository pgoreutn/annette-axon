package axon.rest.bpm.repository

import annette.security.auth.authorization.{AuthorizedActionFactory, CheckAny}
import annette.shared.exceptions.AnnetteException
import axon.bpm.engine.api.{BpmEngineService, ProcessDef}
import axon.bpm.repository.api.BpmRepositoryService
import axon.bpm.repository.api.model._
import axon.knowledge.repository.api.KnowledgeRepositoryService
import axon.knowledge.repository.api.model._
import axon.rest.bpm.permission.BpmPermissions._
import javax.inject.Inject
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.immutable._

class BusinessProcessController @Inject()(
    authorized: AuthorizedActionFactory,
    bpmRepositoryService: BpmRepositoryService,
    bpmEngineService: BpmEngineService,
    knowledgeRepositoryService: KnowledgeRepositoryService,
    cc: ControllerComponents,
    implicit val ec: ExecutionContext)
    extends AbstractController(cc) {

  def find() = authorized(CheckAny(BUSINESS_PROCESS_VIEW)).async(parse.json[FindBusinessProcesss]) { implicit request =>
    val filter = request.body.filter
    (for {
      businessProcesses <- bpmRepositoryService.findBusinessProcess.invoke(filter)
      processDefsByKey <- findProcessDefsByKey(businessProcesses)
      processDefsById <- findProcessDefsById(businessProcesses)
      dataSchemas <- findDataSchemas(businessProcesses)
    } yield {
      val dto = businessProcesses.map { bp =>
        val processReferenceDetail = bp.processReference match {
          case ProcessReferenceByKey(key) =>
            processDefsByKey.get(key).map(pd => ProcessReferenceDetail(key, None, pd.name))
          case ProcessReferenceById(id) =>
            processDefsById.get(id).map(pd => ProcessReferenceDetail(pd.key, Some(pd.version.toString), pd.name))
        }
        val dataSchemaDetail = dataSchemas.get(bp.dataSchemaKey).map(ds => DataSchemaDetail(ds.key, ds.name))
        BusinessProcessSummaryDto(
          key = bp.key,
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

  private def findDataSchemas(businessProcesses: Seq[BusinessProcessSummary]) = {
    knowledgeRepositoryService.findDataSchemaByKeys
      .invoke(businessProcesses.map(_.dataSchemaKey))
      .map(_.map(ds => ds.key -> ds).toMap)
  }

  private def findProcessDefsById(businessProcesses: Seq[BusinessProcessSummary]): Future[Map[String, ProcessDef]] = {

    val ids = businessProcesses.map(_.processReference).flatMap {
      case ProcessReferenceByKey(_) => None
      case ProcessReferenceById(id) => Some(id)
    }
    if (ids.nonEmpty) {
      bpmEngineService.findProcessDefByIds.invoke(ids).map(_.map(pd => pd.id -> pd).toMap)
    } else {
      Future.successful(Map.empty)
    }

  }

  private def findProcessDefsByKey(businessProcesses: Seq[BusinessProcessSummary]): Future[Map[String, ProcessDef]] = {

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

  def buildBusinessProcessDto(businessProcess: BusinessProcess) = {
    for {
      processDef <- findProcessDef(businessProcess.processReference)
      dataSchema <- findDataSchema(businessProcess.dataSchemaKey)
    } yield {

      val processReferenceDetail = businessProcess.processReference match {
        case ProcessReferenceByKey(key) =>
          processDef.map(pd => ProcessReferenceDetail(key, None, pd.name))
        case ProcessReferenceById(id) =>
          processDef.map(pd => ProcessReferenceDetail(pd.key, Some(pd.version.toString), pd.name))
      }
      val dataSchemaDetail = dataSchema.map(ds => DataSchemaDetail(ds.key, ds.name))
      BusinessProcessDto(
        key = businessProcess.key,
        name = businessProcess.name,
        description = businessProcess.description,
        processReference = businessProcess.processReference,
        processReferenceDetail = processReferenceDetail,
        dataSchemaKey = businessProcess.dataSchemaKey,
        dataSchemaDetail = dataSchemaDetail,
        defaults = businessProcess.defaults
      )
    }
  }

  def findByKey(key: String) = authorized(CheckAny(BUSINESS_PROCESS_VIEW)).async { request =>
    {
      for {
        businessProcess <- bpmRepositoryService.findBusinessProcessByKey(key).invoke()
        dto <- buildBusinessProcessDto(businessProcess)
      } yield Ok(Json.toJson(dto))
    }.recover {
      case ex: AnnetteException =>
        BadRequest(ex.toMessage)
    }
  }

  private def findDataSchema(dataSchemaKey: DataSchemaKey) = {
    knowledgeRepositoryService.findDataSchemaByKeys
      .invoke(Seq(dataSchemaKey))
      .map(_.headOption)
  }

  private def findProcessDef(processReference: ProcessReference): Future[Option[ProcessDef]] = {
    processReference match {
      case ProcessReferenceByKey(key) => bpmEngineService.findProcessDefByKeys.invoke(Seq(key)).map(_.headOption)
      case ProcessReferenceById(id)   => bpmEngineService.findProcessDefByIds.invoke(Seq(id)).map(_.headOption)
    }
  }

  def create = authorized(CheckAny(BPM_REPOSITORY_CONTROL)).async(parse.json[BusinessProcess]) { request =>
    val businessProcess = request.body
    (for {
      businessProcess <- bpmRepositoryService.createBusinessProcess.invoke(businessProcess)
      dto <- buildBusinessProcessDto(businessProcess)
    } yield {
      Ok(Json.toJson(dto))
    }).recover {
      case ex: AnnetteException =>
        BadRequest(ex.toMessage)
    }
  }

  def update() = authorized(CheckAny(BPM_REPOSITORY_CONTROL)).async(parse.json[BusinessProcess]) { request =>
    val businessProcess = request.body
    (for {
      businessProcess <- bpmRepositoryService.updateBusinessProcess.invoke(businessProcess)
      dto <- buildBusinessProcessDto(businessProcess)
    } yield {
      Ok(Json.toJson(dto))
    }).recover {
      case ex: AnnetteException =>
        BadRequest(ex.toMessage)
    }
  }

  def delete(key: String) = authorized(CheckAny(BPM_REPOSITORY_CONTROL)).async { request =>
    bpmRepositoryService
      .deleteBusinessProcess(key)
      .invoke()
      .map(_ => Ok(Json.toJson(Map("deleted" -> "true"))))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }

}

case class FindBusinessProcesss(filter: String)

object FindBusinessProcesss {
  implicit val format: Format[FindBusinessProcesss] = Json.format
}

case class BusinessProcessSummaryDto(
    key: BusinessProcessKey,
    name: String,
    description: Option[String],
    processReference: ProcessReference,
    processReferenceDetail: Option[ProcessReferenceDetail],
    dataSchemaKey: DataSchemaKey,
    dataSchemaDetail: Option[DataSchemaDetail],
)

object BusinessProcessSummaryDto {
  implicit val format: Format[BusinessProcessSummaryDto] = Json.format
}

case class BusinessProcessDto(
    key: BusinessProcessKey,
    name: String,
    description: Option[String],
    processReference: ProcessReference,
    processReferenceDetail: Option[ProcessReferenceDetail],
    dataSchemaKey: DataSchemaKey,
    dataSchemaDetail: Option[DataSchemaDetail],
    defaults: Map[String, DataValue],
    // dataSchemaFields: Option[DataSchemaFields],
)

object BusinessProcessDto {
  implicit val format: Format[BusinessProcessDto] = Json.format
}

case class ProcessReferenceDetail(
    key: String,
    version: Option[String],
    name: String,
)

object ProcessReferenceDetail {
  implicit val format: Format[ProcessReferenceDetail] = Json.format
}

case class DataSchemaDetail(
    key: String,
    name: String,
)

object DataSchemaDetail {
  implicit val format: Format[DataSchemaDetail] = Json.format
}
