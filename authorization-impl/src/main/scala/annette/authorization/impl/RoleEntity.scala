/***************************************************************************************
  * Copyright (c) 2014-2017 by Valery Lobachev
  * Redistribution and use in source and binary forms, with or without
  * modification, are NOT permitted without written permission from Valery Lobachev.
  *
  * Copyright (c) 2014-2017 Валерий Лобачев
  * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
  * запрещено без письменного разрешения правообладателя.
****************************************************************************************/
package annette.authorization.impl

import akka.Done
import annette.authorization.api.{Role, RoleAlreadyExist, RoleNotFound}
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
