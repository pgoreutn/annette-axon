/***************************************************************************************
  * Copyright (c) 2014-2017 by Valery Lobachev
  * Redistribution and use in source and binary forms, with or without
  * modification, are NOT permitted without written permission from Valery Lobachev.
  *
  * Copyright (c) 2014-2017 Валерий Лобачев
  * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
  * запрещено без письменного разрешения правообладателя.
****************************************************************************************/
package axon.bpm.repository.impl.process

import akka.Done
import axon.bpm.repository.api.model.BusinessProcess
import axon.bpm.repository.api.{BusinessProcessAlreadyExist, BusinessProcessNotFound}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

class BusinessProcessEntity extends PersistentEntity {
  override type Command = BusinessProcessCommand
  override type Event = BusinessProcessEvent
  override type State = Option[BusinessProcess]
  override def initialState: State = None

  override def behavior: Behavior = {
    case Some(_) =>
      Actions()
        .onReadOnlyCommand[FindBusinessProcessById, Option[BusinessProcess]] {
          case (FindBusinessProcessById(id), ctx, state) => ctx.reply(state)
        }
        .onReadOnlyCommand[CreateBusinessProcess, BusinessProcess] {
          case (CreateBusinessProcess(bp), ctx, state) =>
            ctx.commandFailed(BusinessProcessAlreadyExist(bp.id))
        }
        .onCommand[UpdateBusinessProcess, BusinessProcess] {
          case (UpdateBusinessProcess(businessProcess), ctx, state) =>
            ctx.thenPersist(BusinessProcessUpdated(businessProcess))(_ => ctx.reply(businessProcess))
        }
        .onCommand[DeleteBusinessProcess, Done] {
          case (DeleteBusinessProcess(id), ctx, state) =>
            ctx.thenPersist(BusinessProcessDeleted(id))(_ => ctx.reply(Done))
        }
        .onEvent {
          case (BusinessProcessUpdated(businessProcess), state) =>
            Some(businessProcess)
          case (BusinessProcessDeleted(_), _) =>
            None
        }

    case None =>
      Actions()
        .onCommand[CreateBusinessProcess, BusinessProcess] {
          case (CreateBusinessProcess(businessProcess), ctx, state) =>
            ctx.thenPersist(BusinessProcessCreated(businessProcess))(_ => ctx.reply(businessProcess))
        }
        .onReadOnlyCommand[UpdateBusinessProcess, BusinessProcess] {
          case (UpdateBusinessProcess(businessProcess), ctx, state) =>
            ctx.commandFailed(BusinessProcessNotFound(businessProcess.id))
        }
        .onReadOnlyCommand[DeleteBusinessProcess, Done] {
          case (DeleteBusinessProcess(id), ctx, state) =>
            ctx.commandFailed(BusinessProcessNotFound(id))
        }
        .onReadOnlyCommand[FindBusinessProcessById, Option[BusinessProcess]] {
          case (FindBusinessProcessById(_), ctx, state) =>
            ctx.reply(None)
        }
        .onEvent {
          case (BusinessProcessCreated(businessProcess), state) =>
            Some(businessProcess)
        }
  }

}
