package axon.rest.bpm.config
import annette.security.auth.authentication.AuthenticatedAction
import annette.security.auth.authorization.{AuthorizedActionFactory, CheckAny}
import annette.shared.exceptions.AnnetteException
import axon.bpm.repository.api.{BpmRepositoryService, Schema, SchemaSummary}
import axon.rest.bpm.BpmPermissions._
import javax.inject.Inject
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

class SchemaController @Inject()(
    authenticated: AuthenticatedAction,
    authorized: AuthorizedActionFactory,
    bpmService: BpmRepositoryService,
    cc: ControllerComponents,
    implicit val ec: ExecutionContext)
    extends AbstractController(cc) {

  implicit val findSchemasFormat = Json.format[FindSchemas]
  implicit val schemaSummaryFormat = Json.format[SchemaSummary]
  implicit val schemaFormat = Json.format[Schema]
  implicit val schemaXmlFormat = Json.format[SchemaXML]

  def find() = authorized(CheckAny(VIEW_SCHEMA)).async(parse.json[FindSchemas]) { implicit request =>
    val findSchemas = request.body
    bpmService.findSchemas
      .invoke(findSchemas.filter)
      .map(r => Ok(Json.toJson(r)))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }
  def findById(id: String) = authorized(CheckAny(VIEW_SCHEMA)).async { implicit request =>
    bpmService
      .findSchemaById(id)
      .invoke()
      .map(r => Ok(Json.toJson(r)))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }

  def create = authorized(CheckAny(CREATE_SCHEMA)).async(parse.json[SchemaXML]) { implicit request =>
    val xml = request.body.xml
    bpmService.createSchema
      .invoke(xml)
      .map(r => Ok(Json.toJson(r)))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }
  def update() = authorized(CheckAny(UPDATE_SCHEMA)).async(parse.json[SchemaXML]) { implicit request =>
    val xml = request.body.xml
    bpmService.updateSchema
      .invoke(xml)
      .map(r => Ok(Json.toJson(r)))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }
  def delete(id: String) = authorized(CheckAny(DELETE_SCHEMA)).async { implicit request =>
    bpmService
      .deleteSchema(id)
      .invoke()
      .map(_ => Ok(Json.toJson(Map("deleted" -> "true"))))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }
}

case class FindSchemas(filter: String)
case class SchemaXML(xml: String)
