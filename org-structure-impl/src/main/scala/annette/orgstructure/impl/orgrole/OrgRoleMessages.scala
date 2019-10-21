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

package annette.orgstructure.impl.orgrole

import akka.Done
import annette.orgstructure.api.model.{OrgRole, OrgRoleId}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import play.api.libs.json.{Format, Json}

sealed trait OrgRoleCommand

case class CreateOrgRole(orgRole: OrgRole) extends OrgRoleCommand with ReplyType[OrgRole]
case class UpdateOrgRole(orgRole: OrgRole) extends OrgRoleCommand with ReplyType[OrgRole]
case class DeactivateOrgRole(id: OrgRoleId) extends OrgRoleCommand with ReplyType[Done]
case class ActivateOrgRole(id: OrgRoleId) extends OrgRoleCommand with ReplyType[Done]
case class FindOrgRoleById(id: OrgRoleId) extends OrgRoleCommand with ReplyType[Option[OrgRole]]

object CreateOrgRole {
  implicit val format: Format[CreateOrgRole] = Json.format
}
object UpdateOrgRole {
  implicit val format: Format[UpdateOrgRole] = Json.format
}
object DeactivateOrgRole {
  implicit val format: Format[DeactivateOrgRole] = Json.format
}
object ActivateOrgRole {
  implicit val format: Format[ActivateOrgRole] = Json.format
}
object FindOrgRoleById {
  implicit val format: Format[FindOrgRoleById] = Json.format
}

sealed trait OrgRoleEvent extends AggregateEvent[OrgRoleEvent] {
  override def aggregateTag = OrgRoleEvent.Tag
}

object OrgRoleEvent {
  val NumShards = 4
  val Tag = AggregateEventTag.sharded[OrgRoleEvent](NumShards)
}

case class OrgRoleCreated(orgRole: OrgRole) extends OrgRoleEvent
case class OrgRoleUpdated(orgRole: OrgRole) extends OrgRoleEvent
case class OrgRoleDeactivated(id: OrgRoleId) extends OrgRoleEvent
case class OrgRoleActivated(id: OrgRoleId) extends OrgRoleEvent

object OrgRoleCreated {
  implicit val format: Format[OrgRoleCreated] = Json.format
}
object OrgRoleUpdated {
  implicit val format: Format[OrgRoleUpdated] = Json.format
}
object OrgRoleDeactivated {
  implicit val format: Format[OrgRoleDeactivated] = Json.format
}
object OrgRoleActivated {
  implicit val format: Format[OrgRoleActivated] = Json.format
}


