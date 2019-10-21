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
import akka.actor.ActorSystem
import akka.stream.Materializer
import annette.orgstructure.api._
import annette.orgstructure.api.model._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.collection.immutable._
import scala.concurrent.{ExecutionContext, Future}

class OrgRoleService(
    registry: PersistentEntityRegistry,
    system: ActorSystem,
    orgRoleRepository: OrgRoleRepository,
    elastic: OrgRoleElastic
)(implicit ec: ExecutionContext, mat: Materializer) {

  private def refFor(id: OrgRoleId) = registry.refFor[OrgRoleEntity](id)

  def createOrgRole(orgRole: OrgRole): Future[OrgRole] = {
    refFor(orgRole.id)
      .ask(CreateOrgRole(orgRole))
  }

  def updateOrgRole(orgRole: OrgRole): Future[OrgRole] = {
    refFor(orgRole.id)
      .ask(UpdateOrgRole(orgRole))
  }

  def deactivateOrgRole(id: OrgRoleId): Future[Done] = {
    refFor(id)
      .ask(DeactivateOrgRole(id))
  }

  def activateOrgRole(id: OrgRoleId): Future[Done] = {
    refFor(id)
      .ask(ActivateOrgRole(id))
  }

  def getOrgRoleById(id: OrgRoleId, readSide: Boolean): Future[OrgRole] = {
    val maybeOrgRoleFuture = if (readSide) {
      orgRoleRepository.getOrgRoleById(id)
    } else {
      refFor(id).ask(FindOrgRoleById(id))
    }
    maybeOrgRoleFuture.map(_.getOrElse(throw OrgRoleNotFound(id)))
  }

  def getOrgRolesByIds(ids: Set[OrgRoleId], readSide: Boolean): Future[Set[OrgRole]] = {
    if (readSide) {
      orgRoleRepository.getOrgRolesByIds(ids)
    } else {
      Future.traverse(ids)(id => refFor(id).ask(FindOrgRoleById(id))).map(_.flatten.toSet)
    }
  }

  def findOrgRoles(filter: OrgRoleFilter): Future[OrgRoleFindResult] = {
    elastic.findOrgRoles(filter)
  }

}
