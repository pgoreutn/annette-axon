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
import annette.authorization.api._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

class RoleEntity extends PersistentEntity {
  override type Command = RoleCommand
  override type Event = RoleEvent
  override type State = Option[BaseRole]
  override def initialState = None

  override def behavior: Behavior = {
    case Some(_) =>
      Actions()
        .onReadOnlyCommand[FindRoleById, Option[BaseRole]] {
          case (FindRoleById(id), ctx, state) => ctx.reply(state)
        }
        .onReadOnlyCommand[CreateRole, BaseRole] {
          case (CreateRole(role), ctx, state) =>
            ctx.commandFailed(RoleAlreadyExist(role.id))

        }
        .onReadOnlyCommand[UpdateRole, BaseRole] {
          case (UpdateRole(role), ctx, state) if role.isComposite != state.get.isComposite =>
            ctx.commandFailed(RoleTypeCannotBeChanged(role.id))
        }
        .onCommand[UpdateRole, BaseRole] {
          case (UpdateRole(role), ctx, state) if role.isComposite == state.get.isComposite =>
            ctx.thenPersist(RoleItemsDeleted(role.id))(_ => ())
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
        .onCommand[CreateRole, BaseRole] {
          case (CreateRole(role), ctx, state) =>
            ctx.thenPersist(RoleItemsDeleted(role.id))(_ => ())
            ctx.thenPersist(RoleCreated(role))(_ => ctx.reply(role))
        }
        .onReadOnlyCommand[UpdateRole, BaseRole] {
          case (UpdateRole(role), ctx, state) => ctx.commandFailed(RoleNotFound(role.id))
        }
        .onReadOnlyCommand[DeleteRole, Done] {
          case (DeleteRole(id), ctx, state) => ctx.commandFailed(RoleNotFound(id))
        }
        .onReadOnlyCommand[FindRoleById, Option[BaseRole]] {
          case (FindRoleById(_), ctx, state) => ctx.reply(None)
        }
        .onEvent {
          case (RoleCreated(role), state) =>
            Some(role)
        }
  }

}
