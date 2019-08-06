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

package axon.bpm.repository.impl.diagram

import akka.Done
import axon.bpm.repository.api.model.BpmDiagram
import axon.bpm.repository.api.{BpmDiagramAlreadyExist, BpmDiagramNotFound, NotationChangeProhibited}
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
            state.map(
              s =>
                s.copy(
                  name = bpmDiagram.name,
                  description = bpmDiagram.description,
                  processDefinitions = bpmDiagram.processDefinitions,
                  xml = bpmDiagram.xml
              )
            )
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
