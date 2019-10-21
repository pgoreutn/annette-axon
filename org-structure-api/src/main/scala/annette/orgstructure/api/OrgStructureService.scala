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

package annette.orgstructure.api;

import akka.{Done, NotUsed}
import annette.orgstructure.api.model._
import annette.shared.exceptions.AnnetteExceptionSerializer
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

import scala.collection.immutable._

trait OrgStructureService extends Service {

  def createOrgRole: ServiceCall[OrgRole, OrgRole]
  def updateOrgRole: ServiceCall[OrgRole, OrgRole]
  def deactivateOrgRole(id: OrgRoleId): ServiceCall[NotUsed, Done]
  def activateOrgRole(id: OrgRoleId): ServiceCall[NotUsed, Done]
  def getOrgRoleById(id: OrgRoleId, readSide: Boolean = true): ServiceCall[NotUsed, OrgRole]
  def getOrgRolesByIds(readSide: Boolean = true): ServiceCall[Set[OrgRoleId], Set[OrgRole]]
  def findOrgRoles: ServiceCall[OrgRoleFilter, OrgRoleFindResult]
  
  def createOrgItem(parentId: OrgItemId): ServiceCall[OrgItem, OrgItem]
  def updateOrgItem: ServiceCall[OrgItem, OrgItem]
  def deleteOrgItem(id: OrgItemId): ServiceCall[NotUsed, Done]
  def changeOrgItemOrderPosition(id: OrgItemId, newPosition: Int): ServiceCall[NotUsed, Done]

  def assignOrgRole(orgItemId: OrgItemId, orgRoleId: OrgRoleId): ServiceCall[NotUsed, Done]
  def unassignOrgRole(orgItemId: OrgItemId, orgRoleId: OrgRoleId): ServiceCall[NotUsed, Done]

  def assignUnitAttribute(orgItemId: OrgItemId, attributeKey: OrgAttributeKey): ServiceCall[UnitAttribute, Done]
  def assignPositionAttribute(orgItemId: OrgItemId, attributeKey: OrgAttributeKey): ServiceCall[PositionAttribute, Done]
  def unassignAttribute(orgItemId: OrgItemId, attributeKey: OrgAttributeKey): ServiceCall[NotUsed, Done]

  def assignPerson(orgPositionId: OrgItemId, personId: PersonId): ServiceCall[NotUsed, Done]
  def unassignPerson(orgPositionId: OrgItemId, personId: PersonId): ServiceCall[NotUsed, Done]
  
  def assignHead(orgUnitId: OrgItemId, orgPositionId: OrgItemId): ServiceCall[NotUsed, Done]
  def unassignHead(orgUnitId: OrgItemId, orgPositionId: OrgItemId): ServiceCall[NotUsed, Done]

  def getOrgItemById(id: OrgItemId, readSide: Boolean = true): ServiceCall[NotUsed, OrgItem]
  def getOrgItemsByIds(readSide: Boolean = true): ServiceCall[Set[OrgItemId], Set[OrgItem]]
  def getOrgPositionByPersonId(personId: PersonId): ServiceCall[NotUsed, OrgPosition]

  final override def descriptor = {
    import Service._
    // @formatter:off
    named("org-structure")
      .withCalls(
        restCall(Method.POST, "/api/org-structure/org-role", createOrgRole),
        restCall(Method.PUT, "/api/org-structure/org-role", updateOrgRole),
        restCall(Method.PUT, "/api/org-structure/org-role/:orgRoleId/activate", activateOrgRole _),
        restCall(Method.DELETE, "/api/org-structure/org-role/:orgRoleId/deactivate", deactivateOrgRole _),
        restCall(Method.GET, "/api/org-structure/org-role/:orgRoleId/:readSide", getOrgRoleById _),
        restCall(Method.POST, "/api/org-structure/org-roles/get/:readSide", getOrgRolesByIds _ ),
        restCall(Method.POST, "/api/org-structure/org-roles/find", findOrgRoles ),

        restCall(Method.POST, "/api/org-structure/org-item", createOrgItem _),
        restCall(Method.PUT, "/api/org-structure/org-item", updateOrgItem),
        restCall(Method.DELETE, "/api/org-structure/org-item/:id", deleteOrgItem _),

        restCall(Method.PUT, "/api/org-structure/org-item/:id/position/:newPosition", changeOrgItemOrderPosition _),

        restCall(Method.PUT, "/api/org-structure/org-item/:orgItemId/org-role/:orgRoleId", assignOrgRole _),
        restCall(Method.DELETE, "/api/org-structure/org-item/:orgItemId/org-role/:orgRoleId", unassignOrgRole _),

        restCall(Method.PUT, "/api/org-structure/org-item/:orgItemId/unit-attribute/:attributeKey", assignUnitAttribute _),
        restCall(Method.PUT, "/api/org-structure/org-item/:orgItemId/position-attribute/:attributeKey", assignPositionAttribute _),
        restCall(Method.DELETE, "/api/org-structure/org-item/:orgItemId/attribute/:attributeKey", unassignAttribute _),

        restCall(Method.PUT, "/api/org-structure/org-item/:orgPositionId/person/:personId", assignPerson _),
        restCall(Method.DELETE, "/api/org-structure/org-item/:orgPositionId/person/:personId", unassignPerson _),

        restCall(Method.PUT, "/api/org-structure/org-item/:orgUnitId/head/:orgPositionId", assignPerson _),
        restCall(Method.DELETE, "/api/org-structure/org-item/:orgUnitId/head/:orgPositionId", unassignPerson _),

        restCall(Method.GET, "/api/org-structure/org-item/:id/:readSide", getOrgItemById _),
        restCall(Method.POST, "/api/org-structure/org-items/:readSide", getOrgItemsByIds _ ),

        restCall(Method.GET, "/api/org-structure/org-position/:personId", getOrgPositionByPersonId _),

    )
      .withExceptionSerializer(new AnnetteExceptionSerializer())
      .withAutoAcl(true)
    // @formatter:on
  }
}
