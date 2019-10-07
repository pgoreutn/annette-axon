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

package annette.authorization.impl.assignment

import akka.Done
import annette.authorization.api.model.PrincipalAssignment
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

class AssignmentEntity extends PersistentEntity {
  override type Command = AssignmentCommand
  override type Event = AssignmentEvent
  override type State = Option[PrincipalAssignment]
  override def initialState = None

  override def behavior: Behavior = {
    case Some(_) =>
      Actions()
        .onReadOnlyCommand[AssignPrincipal, Done] {
          case (AssignPrincipal(_), ctx, _) =>
            ctx.reply(Done)
        }
        .onCommand[UnassignPrincipal, Done] {
          case (UnassignPrincipal(assignment), ctx, _) =>
            ctx.thenPersist(PrincipalUnassigned(assignment))(_ => ctx.reply(Done))
        }
        .onEvent {
          case (PrincipalUnassigned(_), _) =>
            None
        }

    case None =>
      Actions()
        .onReadOnlyCommand[UnassignPrincipal, Done] {
          case (UnassignPrincipal(_), ctx, _) =>
            ctx.reply(Done)
        }
        .onCommand[AssignPrincipal, Done] {
          case (AssignPrincipal(assignment), ctx, _) =>
            ctx.thenPersist(PrincipalAssigned(assignment))(_ => ctx.reply(Done))
        }
        .onEvent {
          case (PrincipalAssigned(assignment), _) =>
            Some(assignment)
        }
  }

}
