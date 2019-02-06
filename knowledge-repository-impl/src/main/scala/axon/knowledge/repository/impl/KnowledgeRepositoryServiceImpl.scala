package axon.knowledge.repository.impl

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.{Done, NotUsed}
import axon.knowledge.repository.api._
import axon.knowledge.repository.api.model.{DataSchema, DataSchemaKey, DataSchemaSummary}
import axon.knowledge.repository.impl.schema._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.collection.immutable
import scala.concurrent.ExecutionContext

/**
  * Implementation of the KnowledgeService.
  */
class KnowledgeRepositoryServiceImpl(registry: PersistentEntityRegistry, system: ActorSystem, dataSchemaRepository: DataSchemaRepository)(
    implicit ec: ExecutionContext,
    mat: Materializer)
    extends KnowledgeRepositoryService {

  override def createDataSchema: ServiceCall[DataSchema, DataSchema] = ServiceCall { dataSchema =>
    refFor(dataSchema.key)
      .ask(CreateDataSchema(dataSchema))
  }

  override def updateDataSchema: ServiceCall[DataSchema, DataSchema] = ServiceCall { dataSchema =>
    refFor(dataSchema.key)
      .ask(UpdateDataSchema(dataSchema))
  }

  override def deleteDataSchema(id: DataSchemaKey): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    refFor(id).ask(DeleteDataSchema(id))
  }
  override def findDataSchemaByKey(id: String): ServiceCall[NotUsed, DataSchema] = ServiceCall { _ =>
    refFor(id).ask(FindDataSchemaByKey(id)).map {
      case Some(dataSchema) => dataSchema
      case None             => throw DataSchemaNotFound(id)
    }
  }
  override def findDataSchema: ServiceCall[String, immutable.Seq[DataSchemaSummary]] = ServiceCall { filter =>
    // TODO: temporary solution, should be implemented using ElasticSearch
    dataSchemaRepository.findDataSchemas(filter.trim)
  }

  override def findDataSchemaByKeys: ServiceCall[immutable.Seq[DataSchemaKey], immutable.Seq[DataSchemaSummary]] = { keys =>
    dataSchemaRepository.findDataSchemaByKeys(keys)
  }

  private def refFor(key: DataSchemaKey) = {
    if (key.trim.length != 0) {
      registry.refFor[DataSchemaEntity](key)
    } else {
      throw KeyRequired()
    }
  }

}
