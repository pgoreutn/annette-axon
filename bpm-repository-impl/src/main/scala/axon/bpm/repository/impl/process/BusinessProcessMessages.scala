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
import axon.bpm.repository.api.model.{BusinessProcess, BusinessProcessKey}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import play.api.libs.json.{Format, Json}

sealed trait BusinessProcessCommand

case class CreateBusinessProcess(businessProcess: BusinessProcess) extends BusinessProcessCommand with ReplyType[BusinessProcess]
case class UpdateBusinessProcess(businessProcess: BusinessProcess) extends BusinessProcessCommand with ReplyType[BusinessProcess]
case class DeleteBusinessProcess(key: BusinessProcessKey) extends BusinessProcessCommand with ReplyType[Done]
case class FindBusinessProcessByKey(key: BusinessProcessKey) extends BusinessProcessCommand with ReplyType[Option[BusinessProcess]]

object CreateBusinessProcess {
  implicit val format: Format[CreateBusinessProcess] = Json.format
}
object UpdateBusinessProcess {
  implicit val format: Format[UpdateBusinessProcess] = Json.format
}
object DeleteBusinessProcess {
  implicit val format: Format[DeleteBusinessProcess] = Json.format
}
object FindBusinessProcessByKey {
  implicit val format: Format[FindBusinessProcessByKey] = Json.format
}

sealed trait BusinessProcessEvent extends AggregateEvent[BusinessProcessEvent] {
  override def aggregateTag = BusinessProcessEvent.Tag
}

object BusinessProcessEvent {
  val NumShards = 4
  val Tag = AggregateEventTag.sharded[BusinessProcessEvent](NumShards)
}

case class BusinessProcessCreated(businessProcess: BusinessProcess) extends BusinessProcessEvent
case class BusinessProcessUpdated(businessProcess: BusinessProcess) extends BusinessProcessEvent
case class BusinessProcessDeleted(key: BusinessProcessKey) extends BusinessProcessEvent

object BusinessProcessCreated {
  implicit val format: Format[BusinessProcessCreated] = Json.format
}
object BusinessProcessUpdated {
  implicit val format: Format[BusinessProcessUpdated] = Json.format
}
object BusinessProcessDeleted {
  implicit val format: Format[BusinessProcessDeleted] = Json.format
}
