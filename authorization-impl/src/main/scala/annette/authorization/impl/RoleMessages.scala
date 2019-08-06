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

import akka.Done
import annette.authorization.api.{Role, RoleId}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import play.api.libs.json.{Format, Json}

sealed trait RoleCommand

case class CreateRole(role: Role) extends RoleCommand with ReplyType[Role]
case class UpdateRole(role: Role) extends RoleCommand with ReplyType[Role]
case class DeleteRole(id: RoleId) extends RoleCommand with ReplyType[Done]
case class FindRoleById(id: RoleId) extends RoleCommand with ReplyType[Option[Role]]

object CreateRole {
  implicit val format: Format[CreateRole] = Json.format
}
object UpdateRole {
  implicit val format: Format[UpdateRole] = Json.format
}
object DeleteRole {
  implicit val format: Format[DeleteRole] = Json.format
}
object FindRoleById {
  implicit val format: Format[FindRoleById] = Json.format
}

sealed trait RoleEvent extends AggregateEvent[RoleEvent] {
  override def aggregateTag = RoleEvent.Tag
}

object RoleEvent {
  val NumShards = 4
  val Tag = AggregateEventTag.sharded[RoleEvent](NumShards)
}

case class RoleCreated(role: Role) extends RoleEvent
case class RoleUpdated(role: Role) extends RoleEvent
case class RoleDeleted(id: RoleId) extends RoleEvent
case class RolePermissionDeleted(id: RoleId) extends RoleEvent

object RoleCreated {
  implicit val format: Format[RoleCreated] = Json.format
}
object RoleUpdated {
  implicit val format: Format[RoleUpdated] = Json.format
}
object RoleDeleted {
  implicit val format: Format[RoleDeleted] = Json.format
}

object RolePermissionDeleted {
  implicit val format: Format[RolePermissionDeleted] = Json.format
}
