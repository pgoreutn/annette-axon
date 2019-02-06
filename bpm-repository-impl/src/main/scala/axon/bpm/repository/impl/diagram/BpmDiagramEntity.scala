/***************************************************************************************
  * Copyright (c) 2014-2017 by Valery Lobachev
  * Redistribution and use in source and binary forms, with or without
  * modification, are NOT permitted without written permission from Valery Lobachev.
  *
  * Copyright (c) 2014-2017 Валерий Лобачев
  * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
  * запрещено без письменного разрешения правообладателя.
****************************************************************************************/
package axon.bpm.repository.impl.bpmDiagram

import akka.Done
import axon.bpm.repository.api.{BpmDiagram, BpmDiagramAlreadyExist, BpmDiagramNotFound, NotationChangeProhibited}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

class BpmDiagramEntity extends PersistentEntity {
  override type Command = BpmDiagramCommand
  override type Event = BpmDiagramEvent
  override type State = Option[BpmDiagram]
  override def initialState: State = None

  override def behavior: Behavior = {
    case Some(_) =>
      Actions()
        .onReadOnlyCommand[FindBpmDiagramById, Option[BpmDiagram]] {
          case (FindBpmDiagramById(id), ctx, state) => ctx.reply(state)
        }
        .onReadOnlyCommand[CreateBpmDiagram, BpmDiagram] {
          case (CreateBpmDiagram(BpmDiagram(id, _, _, _, _, _)), ctx, state) =>
            ctx.commandFailed(BpmDiagramAlreadyExist(id))
          //ctx.commandFailed(NotFound(id))
        }
        .onCommand[UpdateBpmDiagram, BpmDiagram] {
          case (UpdateBpmDiagram(bpmDiagram), ctx, state) =>
            // TODO: validate notation
            ctx.thenPersist(BpmDiagramUpdated(bpmDiagram))(_ => ctx.reply(bpmDiagram))
        }
        .onCommand[DeleteBpmDiagram, Done] {
          case (DeleteBpmDiagram(id), ctx, state) =>
            ctx.thenPersist(BpmDiagramDeleted(id))(_ => ctx.reply(Done))
        }
        .onEvent {
          case (BpmDiagramUpdated(bpmDiagram), state) =>
            state.map(s => s.copy(name = bpmDiagram.name, description = bpmDiagram.description, xml = bpmDiagram.xml))
          case (BpmDiagramDeleted(_), _) =>
            None
        }

    case None =>
      Actions()
        .onCommand[CreateBpmDiagram, BpmDiagram] {
          case (CreateBpmDiagram(bpmDiagram), ctx, state) =>
            ctx.thenPersist(BpmDiagramCreated(bpmDiagram))(_ => ctx.reply(bpmDiagram))
        }
        .onReadOnlyCommand[UpdateBpmDiagram, BpmDiagram] {
          case (UpdateBpmDiagram(BpmDiagram(id, _, _, _, _, _)), ctx, state) => ctx.commandFailed(BpmDiagramNotFound(id))
        }
        .onReadOnlyCommand[DeleteBpmDiagram, Done] {
          case (DeleteBpmDiagram(id), ctx, state) => ctx.commandFailed(BpmDiagramNotFound(id))
        }
        .onReadOnlyCommand[FindBpmDiagramById, Option[BpmDiagram]] {
          case (FindBpmDiagramById(_), ctx, state) => ctx.reply(None)
        }
        .onEvent {
          case (BpmDiagramCreated(bpmDiagram), state) =>
            Some(bpmDiagram)
        }
  }

}
