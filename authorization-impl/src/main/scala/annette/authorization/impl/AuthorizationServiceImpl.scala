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

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.{Done, NotUsed}
import annette.authorization.api._
import annette.authorization.api.model.{CheckPermissions, _}
import annette.authorization.impl.assignment.UserRoleAssignmentRepository
import annette.authorization.impl.role._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.collection._
import scala.concurrent.ExecutionContext

class AuthorizationServiceImpl(
                                registry: PersistentEntityRegistry,
                                system: ActorSystem,
                                roleService: RoleService,
                                userRoleAssignmentRepository: UserRoleAssignmentRepository
                              )(implicit ec: ExecutionContext, mat: Materializer)
  extends AuthorizationService {

  override def createRole: ServiceCall[Role, Role] = ServiceCall { role =>
    roleService.createRole(role)
  }
  override def updateRole: ServiceCall[Role, Role] = ServiceCall { role =>
    roleService.updateRole(role)
  }
  override def deleteRole(id: RoleId): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    roleService.deleteRole(id)
  }

  override def findRoleById(id: RoleId): ServiceCall[NotUsed, Role] = ServiceCall { _ =>
    roleService.findRoleById(id)
  }

  override def findRoles: ServiceCall[RoleFilter, RoleFindResult] = ServiceCall { filter =>
    roleService.findRoles(filter)
  }

  override def checkAllPermissions: ServiceCall[CheckPermissions, Boolean] = ServiceCall { checkPermissions =>
    roleService.checkAllPermissions(checkPermissions)
  }

  override def checkAnyPermissions: ServiceCall[CheckPermissions, Boolean] = ServiceCall { checkPermissions =>
    roleService.checkAnyPermissions(checkPermissions)
  }

  override def findPermissions: ServiceCall[FindPermissions, immutable.Set[Permission]] = ServiceCall { findPermissions =>
    roleService.findPermissions(findPermissions)
  }

  override def assignUserToRoles(userId: UserId): ServiceCall[immutable.Set[RoleId], Done] = ServiceCall { set =>
    // TODO: validate if all roles are exist
    userRoleAssignmentRepository.assignUserToRoles(userId, set)
  }

  override def unassignUserFromRoles(userId: UserId): ServiceCall[immutable.Set[RoleId], Done] = ServiceCall { set =>
    userRoleAssignmentRepository.unassignUserFromRoles(userId, set)
  }

  override def findRolesAssignedToUser(userId: UserId): ServiceCall[NotUsed, immutable.Set[RoleId]] = ServiceCall { _ =>
    userRoleAssignmentRepository.findRolesAssignedToUser(userId)
  }

  override def findUsersAssignedToRole(roleId: RoleId): ServiceCall[NotUsed, immutable.Set[UserId]] = ServiceCall { _ =>
    userRoleAssignmentRepository.findUsersAssignedToRole(roleId)
  }
}
