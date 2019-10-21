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
import annette.orgstructure.api.{OrgRoleAlreadyExist, OrgRoleNotFound}
import annette.orgstructure.api.model._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

class OrgRoleEntity extends PersistentEntity {
  override type Command = OrgRoleCommand
  override type Event = OrgRoleEvent
  override type State = Option[OrgRole]
  override def initialState = None

  override def behavior: Behavior = {
    case Some(_) =>
      Actions()
        .onReadOnlyCommand[FindOrgRoleById, Option[OrgRole]] {
          case (FindOrgRoleById(id), ctx, state) => ctx.reply(state)
        }
        .onReadOnlyCommand[CreateOrgRole, OrgRole] {
          case (CreateOrgRole(OrgRole(id, _, _, _, _)), ctx, state) =>
            ctx.commandFailed(OrgRoleAlreadyExist(id))
        }
        .onCommand[UpdateOrgRole, OrgRole] {
          case (UpdateOrgRole(orgRole), ctx, state) =>
            ctx.thenPersist(OrgRoleUpdated(orgRole))(_ => ctx.reply(orgRole))
        }
        .onCommand[DeactivateOrgRole, Done] {
          case (DeactivateOrgRole(id), ctx, state) =>
            ctx.thenPersist(OrgRoleDeactivated(id))(_ => ctx.reply(Done))
        }
        .onEvent {
          case (OrgRoleUpdated(orgRole), state) =>
            state.map(s => orgRole)
          case (OrgRoleDeactivated(_), _) =>
            None
        }

    case None =>
      Actions()
        .onCommand[CreateOrgRole, OrgRole] {
          case (CreateOrgRole(orgRole), ctx, state) =>
            ctx.thenPersist(OrgRoleCreated(orgRole))(_ => ctx.reply(orgRole))
        }
        .onReadOnlyCommand[UpdateOrgRole, OrgRole] {
          case (UpdateOrgRole(OrgRole(id, _, _, _, _)), ctx, state) => ctx.commandFailed(OrgRoleNotFound(id))
        }
        .onReadOnlyCommand[DeactivateOrgRole, Done] {
          case (DeactivateOrgRole(id), ctx, state) => ctx.commandFailed(OrgRoleNotFound(id))
        }
        .onReadOnlyCommand[FindOrgRoleById, Option[OrgRole]] {
          case (FindOrgRoleById(_), ctx, state) => ctx.reply(None)
        }
        .onEvent {
          case (OrgRoleCreated(orgRole), state) =>
            Some(orgRole)
        }
  }

}
