package axon.bpm.repository.impl

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.{Done, NotUsed}
import axon.bpm.repository.api._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.{BadRequest, NotFound}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.xml.XML

/**
  * Implementation of the BpmService.
  */
class BpmRepositoryServiceImpl(registry: PersistentEntityRegistry, system: ActorSystem, schemaRepository: SchemaRepository)(
    implicit ec: ExecutionContext,
    mat: Materializer)
    extends BpmRepositoryService {

  override def createSchema(notation: String): ServiceCall[String, Done] = ServiceCall { schema =>
    val (id, name, description) = parseXml(schema)
    refFor(id)
      .ask(CreateSchema(id, name, Some(description), notation, schema))
  }

  override def updateSchema: ServiceCall[String, Done] = ServiceCall { schema =>
    val (id, name, description) = parseXml(schema)
    refFor(id)
      .ask(UpdateSchema(id, name, Some(description), schema))
  }

  override def deleteSchema(id: SchemaId): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    refFor(id).ask(DeleteSchema(id))
  }
  override def findSchemaById(id: String): ServiceCall[NotUsed, Schema] = ServiceCall { _ =>
    refFor(id).ask(FindSchemaById(id)).map {
      case Some(schema) => schema
      case None         => throw SchemaNotFound(id)
    }
  }
  override def findSchemas: ServiceCall[String, immutable.Seq[SchemaSummary]] = ServiceCall { filter =>
    // TODO: temporary solution, should be implemented using ElasticSearch
    schemaRepository.findSchemas(filter.trim)
  }

  private def refFor(id: SchemaId) = registry.refFor[SchemaEntity](id)

  private def parseXml(schema: String): (String, String, String) = {
    Try {
      val xml = XML.loadString(schema)
      val id = (xml \\ "process" \ "@id").text
      val name = (xml \\ "process" \ "@name").text
      val description = (xml \\ "process" \ "documentation").text
      (id, name, description)
    }.getOrElse(throw XmlParseError())
  }

}
