package axon.bpm.repository.impl

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.{Done, NotUsed}
import axon.bpm.repository.impl.schema._
import axon.bpm.repository.api._
import axon.bpm.repository.impl.schema._
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

  override def createSchema: ServiceCall[Schema, Schema] = ServiceCall { schema =>
    refFor(schema.id)
      .ask(CreateSchema(schema))
  }

  override def updateSchema: ServiceCall[Schema, Schema] = ServiceCall { schema =>
    refFor(schema.id)
      .ask(UpdateSchema(schema))
  }

  override def deleteSchema(id: SchemaId): ServiceCall[NotUsed, Done] = ServiceCall { _ => refFor(id).ask(DeleteSchema(id))
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

//  private def parseXml(schema: String): Schema = {
//    val xml = Try(XML.loadString(schema)).getOrElse(throw XmlParseError())
//    println(xml)
//    val r = Try {
//      val id = (xml \\ "process" \ "@id").text
//      if (id.trim.isEmpty) throw XmlParseError()
//      val name = (xml \\ "process" \ "@name").text
//      val description = (xml \\ "process" \ "documentation").text
//      val descriptionOpt = if (description.isEmpty) None else Some(description)
//      val notation = "BPMN"
//      Schema(id, name, descriptionOpt, notation, schema)
//    }.getOrElse(throw XmlParseError())
//    println(r)
//    r
//  }

}
