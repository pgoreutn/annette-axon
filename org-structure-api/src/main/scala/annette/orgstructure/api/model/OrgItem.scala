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

package annette.orgstructure.api.model

import java.time.OffsetDateTime

import play.api.libs.json.{Format, Json, JsonConfiguration, JsonNaming, OFormat, Reads, Writes}

sealed trait OrgItem {
  val id: OrgItemId
  val name: String
  val shortName: String
  val description: Option[String]
  val parent: Option[OrgItemId]
  val orgRoles: Map[OrgRoleId, OrgRoleAssignmentStatus.OrgRoleAssignmentStatus]
  val updatedAt: OffsetDateTime
}

case class OrgPosition(
    id: OrgItemId,
    name: String,
    shortName: String,
    description: Option[String],
    personId: Option[PersonId],
    parent: Option[OrgItemId],
    orgRoles: Map[OrgRoleId, OrgRoleAssignmentStatus.OrgRoleAssignmentStatus],
    attributes: Map[OrgAttributeKey, PositionAttribute],
    updatedAt: OffsetDateTime = OffsetDateTime.now()
) extends OrgItem

case class OrgUnit(
    id: OrgItemId,
    name: String,
    shortName: String,
    description: Option[String],
    headId: Option[OrgItemId],
    parent: Option[OrgItemId],
    children: Seq[OrgItemId],
    orgRoles: Map[OrgRoleId, OrgRoleAssignmentStatus.OrgRoleAssignmentStatus],
    attributes: Map[OrgAttributeKey, UnitAttribute],
    updatedAt: OffsetDateTime = OffsetDateTime.now()
) extends OrgItem

object OrgUnit {
  implicit val format: Format[OrgUnit] = Json.format
}

object OrgPosition {
  implicit val format: Format[OrgPosition] = Json.format
}

object OrgItem {
  implicit val config = JsonConfiguration(discriminator = "itemType", typeNaming = JsonNaming { fullName =>
    fullName.split("\\.").toSeq.last
  })

  implicit val format: OFormat[OrgItem] = Json.format[OrgItem]
}
