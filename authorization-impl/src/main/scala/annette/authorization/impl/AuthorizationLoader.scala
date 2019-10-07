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

package annette.authorization.impl

import annette.authorization.api._
import annette.authorization.impl.assignment.{
  AssignmentEntity,
  AssignmentEventProcessor,
  AssignmentRepository,
  AssignmentSerializerRegistry,
  AssignmentService
}
import annette.authorization.impl.role.{RoleElastic, RoleEntity, RoleEventProcessor, RoleRepository, RoleSerializerRegistry, RoleService}
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

  lazy val elasticClient = wireWith(ElasticProvider.create _)
  lazy val roleRepository = wire[RoleRepository]
  lazy val roleElastic = wire[RoleElastic]
  lazy val roleService = wire[RoleService]

  persistentEntityRegistry.register(wire[RoleEntity])
  readSide.register(wire[RoleEventProcessor])

  lazy val assignmentRepository = wire[AssignmentRepository]
  lazy val assignmentService = wire[AssignmentService]

  persistentEntityRegistry.register(wire[AssignmentEntity])
  readSide.register(wire[AssignmentEventProcessor])

  lazy val jsonSerializerRegistry = AuthorizationSerializerRegistry
}

object AuthorizationSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] = RoleSerializerRegistry.serializers ++ AssignmentSerializerRegistry.serializers
}
