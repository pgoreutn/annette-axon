package axon.rest.knowledge.repository
import annette.security.auth.authentication.AuthenticatedAction
import annette.security.auth.authorization.{AuthorizedActionFactory, CheckAny}
import annette.shared.exceptions.AnnetteException
import axon.knowledge.repository.api.KnowledgeRepositoryService
import axon.knowledge.repository.api.model.{DataSchema, DataSchemaSummary}
import axon.rest.knowledge.permission.KnowledgePermissions._
import javax.inject.Inject
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.collection.immutable
import scala.concurrent.ExecutionContext

class DataSchemaController @Inject()(
                                      authenticated: AuthenticatedAction,
                                      authorized: AuthorizedActionFactory,
                                      knowledgeRepositoryService: KnowledgeRepositoryService,
                                      cc: ControllerComponents,
                                      implicit val ec: ExecutionContext)
  extends AbstractController(cc) {

  implicit val findDataSchemasFormat = Json.format[FindDataSchemas]
  implicit val dataSchemaSummaryFormat = Json.format[DataSchemaSummary]
  implicit val dataSchemaFormat = Json.format[DataSchema]

  def find() = authorized(CheckAny(VIEW_DATA_SCHEMA)).async(parse.json[FindDataSchemas]) { implicit request =>
    val findDataSchemas = request.body
    knowledgeRepositoryService.findDataSchema
      .invoke(findDataSchemas.filter)
      .map(r => Ok(Json.toJson(r)))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }

  def findByKeys() = authorized(CheckAny(VIEW_DATA_SCHEMA)).async(parse.json[immutable.Seq[String]]) { implicit request =>
    val keys = request.body
    knowledgeRepositoryService.findDataSchemaByKeys
      .invoke(keys)
      .map(r => Ok(Json.toJson(r)))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }

  def findByKey(key: String) = authorized(CheckAny(VIEW_DATA_SCHEMA)).async { implicit request =>
    knowledgeRepositoryService
      .findDataSchemaByKey(key)
      .invoke()
      .map(r => Ok(Json.toJson(r)))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }

  def normalizeFields(dataSchema: DataSchema): DataSchema = {
    val  fields = dataSchema.fields.values.map(f => f.key -> f).toMap
    dataSchema.copy(fields = fields)
  }

  def create = authorized(CheckAny(CREATE_DATA_SCHEMA)).async(parse.json[DataSchema]) { implicit request =>
    val dataSchema = normalizeFields(request.body)
    knowledgeRepositoryService.createDataSchema
      .invoke(dataSchema)
      .map(r => Ok(Json.toJson(r)))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }
  def update() = authorized(CheckAny(UPDATE_DATA_SCHEMA)).async(parse.json[DataSchema]) { implicit request =>
    val dataSchema = normalizeFields(request.body)
    knowledgeRepositoryService.updateDataSchema
      .invoke(dataSchema)
      .map(r => Ok(Json.toJson(r)))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }
  def delete(key: String) = authorized(CheckAny(DELETE_DATA_SCHEMA)).async { implicit request =>
    knowledgeRepositoryService
      .deleteDataSchema(key)
      .invoke()
      .map(_ => Ok(Json.toJson(Map("deleted" -> "true"))))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }
}

case class FindDataSchemas(filter: String)
