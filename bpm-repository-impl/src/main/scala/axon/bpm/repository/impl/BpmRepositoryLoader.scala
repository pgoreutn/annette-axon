package axon.bpm.repository.impl

import axon.bpm.repository.api.BpmRepositoryService
import axon.bpm.repository.impl.schema.{SchemaEntity, SchemaEventProcessor, SchemaRepository, SchemaSerializerRegistry}
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable

class BpmRepositoryLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new BpmRepositoryApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new BpmRepositoryApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[BpmRepositoryService])
}

abstract class BpmRepositoryApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  override lazy val lagomServer = serverFor[BpmRepositoryService](wire[BpmRepositoryServiceImpl])
  lazy val schemaRepository = wire[SchemaRepository]
  lazy val jsonSerializerRegistry = BpmRepositorySerializerRegistry

  persistentEntityRegistry.register(wire[SchemaEntity])
  readSide.register(wire[SchemaEventProcessor])

}

object BpmRepositorySerializerRegistry extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] = SchemaSerializerRegistry.serializers
}
