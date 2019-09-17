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

package annette.authorization.impl.role

import akka.Done
import akka.actor.ActorSystem
import akka.stream.Materializer
import annette.authorization.api._
import annette.authorization.api.model._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.collection._
import scala.concurrent.{ExecutionContext, Future}

class RoleService(
    registry: PersistentEntityRegistry,
    system: ActorSystem,
    roleRepository: RoleRepository,
    elastic: RoleElastic
)(implicit ec: ExecutionContext, mat: Materializer) {

  private def refFor(id: RoleId) = registry.refFor[RoleEntity](id)

  def createRole(role: Role): Future[Role] = {
    refFor(role.id)
      .ask(CreateRole(role))
  }
  def updateRole(role: Role): Future[Role] = {
    refFor(role.id)
      .ask(UpdateRole(role))
  }
  def deleteRole(id: RoleId): Future[Done] = {
    refFor(id)
      .ask(DeleteRole(id))
  }

  def findRoleById(id: RoleId): Future[Role] = {
    refFor(id).ask(FindRoleById(id)).map {
      case Some(role) => role
      case None       => throw RoleNotFound(id)
    }
  }

  def findRoles(filter: RoleFilter): Future[RoleFindResult] = {
    elastic.findRoles(filter).flatMap { res =>
      val ids = res.hits.map(_.id)
      for {
        tags <- findRolesByIds(ids)
        tagMap = tags.map(tag => tag.id -> tag).toMap
      } yield {
        val hits = res.hits.map(hit => hit.copy(data = tagMap.get(hit.id)))
        res.copy(hits = hits)
      }
    }
  }

  def findRolesByIds(ids: Seq[RoleId]): Future[Seq[Role]] = {
    Future.traverse(ids)(id => refFor(id).ask(FindRoleById(id))).map(_.flatten)
  }

  def checkAllPermissions: PartialFunction[CheckPermissions, Future[Boolean]] = {
    case CheckPermissions(roles, permissions) =>
      if (roles.isEmpty || permissions.isEmpty) {
        Future.successful(false)
      } else {
        roleRepository.checkAllPermissions(roles, permissions)
      }
  }

  def checkAnyPermissions: PartialFunction[CheckPermissions, Future[Boolean]] = {
    case CheckPermissions(roles, permissions) =>
      if (roles.isEmpty || permissions.isEmpty) {
        Future.successful(false)
      } else {
        roleRepository.checkAnyPermissions(roles, permissions)
      }
  }

  def findPermissions: PartialFunction[FindPermissions, Future[immutable.Set[Permission]]] = {
    case FindPermissions(roles, permissionIds) =>
      if (roles.isEmpty || permissionIds.isEmpty) {
        Future.successful(immutable.Set.empty)
      } else {
        roleRepository.findPermissions(roles, permissionIds)
      }
  }

}
