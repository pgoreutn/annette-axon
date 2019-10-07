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
import annette.authorization.api.model.{AuthorizationPrincipal, PrincipalAssignment, RoleId}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import play.api.libs.json.{Format, Json}

sealed trait AssignmentCommand

case class AssignPrincipal(assignment: PrincipalAssignment) extends AssignmentCommand with ReplyType[Done]
case class UnassignPrincipal(assignment: PrincipalAssignment) extends AssignmentCommand with ReplyType[Done]

object AssignPrincipal {
  implicit val format: Format[AssignPrincipal] = Json.format
}
object UnassignPrincipal {
  implicit val format: Format[UnassignPrincipal] = Json.format
}

sealed trait AssignmentEvent extends AggregateEvent[AssignmentEvent] {
  override def aggregateTag = AssignmentEvent.Tag
}

object AssignmentEvent {
  val NumShards = 4
  val Tag = AggregateEventTag.sharded[AssignmentEvent](NumShards)
}

case class PrincipalAssigned(assignment: PrincipalAssignment) extends AssignmentEvent
case class PrincipalUnassigned(assignment: PrincipalAssignment) extends AssignmentEvent

object PrincipalAssigned {
  implicit val format: Format[PrincipalAssigned] = Json.format
}
object PrincipalUnassigned {
  implicit val format: Format[PrincipalUnassigned] = Json.format
}

