package annette.authorization.impl

import annette.authorization.api._
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable

class AuthorizationLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new AuthorizationApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new AuthorizationApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[AuthorizationService])
}

abstract class AuthorizationApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  override lazy val lagomServer = serverFor[AuthorizationService](wire[AuthorizationServiceImpl])

  lazy val roleRepository = wire[RoleRepository]
  lazy val jsonSerializerRegistry = AuthorizationSerializerRegistry

  persistentEntityRegistry.register(wire[RoleEntity])
  readSide.register(wire[RoleEventProcessor])

}

object AuthorizationSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] = List(
    JsonSerializer[Permission],
    JsonSerializer[BaseRole],
    JsonSerializer[CheckPermissions],
    JsonSerializer[FindPermissions],
    JsonSerializer[CreateRole],
    JsonSerializer[UpdateRole],
    JsonSerializer[DeleteRole],
    JsonSerializer[RoleCreated],
    JsonSerializer[RoleUpdated],
    JsonSerializer[RoleDeleted],
    JsonSerializer[RoleItemsDeleted]
  )
}
