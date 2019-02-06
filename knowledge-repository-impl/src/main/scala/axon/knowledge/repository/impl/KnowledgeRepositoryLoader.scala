package axon.knowledge.repository.impl

import axon.knowledge.repository.api.KnowledgeRepositoryService
import axon.knowledge.repository.impl.schema.{DataSchemaEntity, DataSchemaEventProcessor, DataSchemaRepository, DataSchemaSerializerRegistry}
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable

class KnowledgeRepositoryLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new KnowledgeRepositoryApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new KnowledgeRepositoryApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[KnowledgeRepositoryService])
}

abstract class KnowledgeRepositoryApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  override lazy val lagomServer = serverFor[KnowledgeRepositoryService](wire[KnowledgeRepositoryServiceImpl])
  lazy val dataSchemaRepository = wire[DataSchemaRepository]
  lazy val jsonSerializerRegistry = KnowledgeRepositorySerializerRegistry

  persistentEntityRegistry.register(wire[DataSchemaEntity])
  readSide.register(wire[DataSchemaEventProcessor])

}

object KnowledgeRepositorySerializerRegistry extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] = DataSchemaSerializerRegistry.serializers
}
