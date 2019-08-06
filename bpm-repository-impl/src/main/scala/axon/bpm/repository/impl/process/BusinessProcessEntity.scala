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
        .onReadOnlyCommand[FindBusinessProcessByKey, Option[BusinessProcess]] {
          case (FindBusinessProcessByKey(id), ctx, state) => ctx.reply(state)
        }
        .onReadOnlyCommand[CreateBusinessProcess, BusinessProcess] {
          case (CreateBusinessProcess(bp), ctx, state) =>
            ctx.commandFailed(BusinessProcessAlreadyExist(bp.key))
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
            ctx.commandFailed(BusinessProcessNotFound(businessProcess.key))
        }
        .onReadOnlyCommand[DeleteBusinessProcess, Done] {
          case (DeleteBusinessProcess(id), ctx, state) =>
            ctx.commandFailed(BusinessProcessNotFound(id))
        }
        .onReadOnlyCommand[FindBusinessProcessByKey, Option[BusinessProcess]] {
          case (FindBusinessProcessByKey(_), ctx, state) =>
            ctx.reply(None)
        }
        .onEvent {
          case (BusinessProcessCreated(businessProcess), state) =>
            Some(businessProcess)
        }
  }

}
