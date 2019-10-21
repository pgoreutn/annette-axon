/*
 * Copyright 2018 Valery Lobachev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package annette.orgstructure.impl

import annette.orgstructure.api._
import annette.orgstructure.impl.orgrole.{OrgRoleElastic, OrgRoleEntity, OrgRoleEventProcessor, OrgRoleRepository, OrgRoleSerializerRegistry, OrgRoleService}
import annette.shared.elastic.ElasticProvider
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable

class OrgStructureLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new OrgStructureApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new OrgStructureApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[OrgStructureService])
}

abstract class OrgStructureApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  override lazy val lagomServer = serverFor[OrgStructureService](wire[OrgStructureServiceImpl])

  lazy val elasticClient = wireWith(ElasticProvider.create _)
  lazy val orgRoleRepository = wire[OrgRoleRepository]
  lazy val orgRoleElastic = wire[OrgRoleElastic]
  lazy val orgRoleService = wire[OrgRoleService]

  persistentEntityRegistry.register(wire[OrgRoleEntity])
  readSide.register(wire[OrgRoleEventProcessor])


  lazy val jsonSerializerRegistry = OrgStructureSerializerRegistry
}

object OrgStructureSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] = OrgRoleSerializerRegistry.serializers
}
