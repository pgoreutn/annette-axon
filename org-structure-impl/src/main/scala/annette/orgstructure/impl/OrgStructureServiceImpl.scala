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

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.{Done, NotUsed}
import annette.orgstructure.api.OrgStructureService
import annette.orgstructure.api.model.{OrgAttributeKey, OrgItem, OrgItemId, OrgPosition, OrgRole, OrgRoleFilter, OrgRoleFindResult, OrgRoleId, PersonId, PositionAttribute, UnitAttribute}
import annette.orgstructure.impl.orgrole.OrgRoleService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.collection.immutable._
import scala.concurrent.ExecutionContext

class OrgStructureServiceImpl(
    registry: PersistentEntityRegistry,
    system: ActorSystem,
    orgRoleService: OrgRoleService,
)(implicit ec: ExecutionContext, mat: Materializer)
    extends OrgStructureService {
  override def createOrgRole: ServiceCall[OrgRole, OrgRole] = ServiceCall { orgRole =>
      orgRoleService.createOrgRole(orgRole)
  }

  override def updateOrgRole: ServiceCall[OrgRole, OrgRole] = ServiceCall { orgRole =>
    orgRoleService.updateOrgRole(orgRole)
  }

  override def deactivateOrgRole(id: OrgRoleId): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    orgRoleService.deactivateOrgRole(id)
  }

  override def activateOrgRole(id: OrgRoleId): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    orgRoleService.activateOrgRole(id)
  }

  override def getOrgRoleById(id: OrgRoleId, readSide: Boolean): ServiceCall[NotUsed, OrgRole] = ServiceCall { _ =>
    orgRoleService.getOrgRoleById(id, readSide)
  }

  override def getOrgRolesByIds(readSide: Boolean): ServiceCall[Set[OrgRoleId], Set[OrgRole]] = ServiceCall { ids =>
    orgRoleService.getOrgRolesByIds(ids, readSide)
  }

  override def findOrgRoles: ServiceCall[OrgRoleFilter, OrgRoleFindResult] = ServiceCall { filter =>
    orgRoleService.findOrgRoles(filter)
  }

  override def createOrgItem(parentId: OrgItemId): ServiceCall[OrgItem, OrgItem] = ???

  override def updateOrgItem: ServiceCall[OrgItem, OrgItem] = ???

  override def deleteOrgItem(id: OrgItemId): ServiceCall[NotUsed, Done] = ???

  override def changeOrgItemOrderPosition(id: OrgItemId, newPosition: Int): ServiceCall[NotUsed, Done] = ???

  override def assignOrgRole(orgItemId: OrgItemId, orgRoleId: OrgRoleId): ServiceCall[NotUsed, Done] = ???

  override def unassignOrgRole(orgItemId: OrgItemId, orgRoleId: OrgRoleId): ServiceCall[NotUsed, Done] = ???

  override def assignUnitAttribute(orgItemId: OrgItemId, attributeKey: OrgAttributeKey): ServiceCall[UnitAttribute, Done] = ???

  override def assignPositionAttribute(orgItemId: OrgItemId, attributeKey: OrgAttributeKey): ServiceCall[PositionAttribute, Done] = ???

  override def unassignAttribute(orgItemId: OrgItemId, attributeKey: OrgAttributeKey): ServiceCall[NotUsed, Done] = ???

  override def assignPerson(orgPositionId: OrgItemId, personId: PersonId): ServiceCall[NotUsed, Done] = ???

  override def unassignPerson(orgPositionId: OrgItemId, personId: PersonId): ServiceCall[NotUsed, Done] = ???

  override def assignHead(orgUnitId: OrgItemId, orgPositionId: OrgItemId): ServiceCall[NotUsed, Done] = ???

  override def unassignHead(orgUnitId: OrgItemId, orgPositionId: OrgItemId): ServiceCall[NotUsed, Done] = ???

  override def getOrgItemById(id: OrgItemId, readSide: Boolean): ServiceCall[NotUsed, OrgItem] = ???

  override def getOrgItemsByIds(readSide: Boolean): ServiceCall[Set[OrgItemId], Set[OrgItem]] = ???

  override def getOrgPositionByPersonId(personId: PersonId): ServiceCall[NotUsed, OrgPosition] = ???
}
