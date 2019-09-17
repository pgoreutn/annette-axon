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
import annette.authorization.api.{RoleAlreadyExist, RoleNotFound}
import annette.authorization.api.model._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

class RoleEntity extends PersistentEntity {
  override type Command = RoleCommand
  override type Event = RoleEvent
  override type State = Option[Role]
  override def initialState = None

  override def behavior: Behavior = {
    case Some(_) =>
      Actions()
        .onReadOnlyCommand[FindRoleById, Option[Role]] {
          case (FindRoleById(id), ctx, state) => ctx.reply(state)
        }
        .onReadOnlyCommand[CreateRole, Role] {
          case (CreateRole(Role(id, _, _, _)), ctx, state) =>
            ctx.commandFailed(RoleAlreadyExist(id))
        }
        .onCommand[UpdateRole, Role] {
          case (UpdateRole(role), ctx, state) =>
            ctx.thenPersist(RolePermissionDeleted(role.id))(_ => ())
            ctx.thenPersist(RoleUpdated(role))(_ => ctx.reply(role))
        }
        .onCommand[DeleteRole, Done] {
          case (DeleteRole(id), ctx, state) =>
            ctx.thenPersist(RoleDeleted(id))(_ => ctx.reply(Done))
        }
        .onEvent {
          case (RoleUpdated(role), state) =>
            state.map(s => role)
          case (RoleDeleted(_), _) =>
            None
        }

    case None =>
      Actions()
        .onCommand[CreateRole, Role] {
          case (CreateRole(role), ctx, state) =>
            ctx.thenPersist(RolePermissionDeleted(role.id))(_ => ())
            ctx.thenPersist(RoleCreated(role))(_ => ctx.reply(role))
        }
        .onReadOnlyCommand[UpdateRole, Role] {
          case (UpdateRole(Role(id, _, _, _)), ctx, state) => ctx.commandFailed(RoleNotFound(id))
        }
        .onReadOnlyCommand[DeleteRole, Done] {
          case (DeleteRole(id), ctx, state) => ctx.commandFailed(RoleNotFound(id))
        }
        .onReadOnlyCommand[FindRoleById, Option[Role]] {
          case (FindRoleById(_), ctx, state) => ctx.reply(None)
        }
        .onEvent {
          case (RoleCreated(role), state) =>
            Some(role)
        }
  }

}
