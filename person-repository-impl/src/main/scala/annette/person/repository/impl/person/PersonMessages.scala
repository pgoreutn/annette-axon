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

package annette.person.repository.impl.person

import akka.Done
import annette.person.repository.api.model.{Person, PersonId}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import play.api.libs.json.{Format, Json}

sealed trait PersonCommand

case class ImportPerson(person: Person) extends PersonCommand with ReplyType[Person]
case class CreatePerson(person: Person) extends PersonCommand with ReplyType[Person]
case class UpdatePerson(person: Person) extends PersonCommand with ReplyType[Person]
case class DeactivatePerson(id: PersonId) extends PersonCommand with ReplyType[Done]
case class ActivatePerson(id: PersonId) extends PersonCommand with ReplyType[Person]
case class GetPersonById(id: PersonId) extends PersonCommand with ReplyType[Option[Person]]

object ImportPerson {
  implicit val format: Format[ImportPerson] = Json.format
}
object CreatePerson {
  implicit val format: Format[CreatePerson] = Json.format
}
object UpdatePerson {
  implicit val format: Format[UpdatePerson] = Json.format
}
object DeactivatePerson {
  implicit val format: Format[DeactivatePerson] = Json.format
}
object ActivatePerson {
  implicit val format: Format[ActivatePerson] = Json.format
}
object GetPersonById {
  implicit val format: Format[GetPersonById] = Json.format
}

sealed trait PersonEvent extends AggregateEvent[PersonEvent] {
  override def aggregateTag = PersonEvent.Tag
}

object PersonEvent {
  val NumShards = 32
  val Tag = AggregateEventTag.sharded[PersonEvent](NumShards)
}

case class PersonImported(person: Person) extends PersonEvent
case class PersonCreated(person: Person) extends PersonEvent
case class PersonUpdated(person: Person) extends PersonEvent
case class PersonDeactivated(person: Person) extends PersonEvent
case class PersonActivated(person: Person) extends PersonEvent
case class PersonIndexed(person: Person) extends PersonEvent

object PersonImported {
  implicit val format: Format[PersonImported] = Json.format
}
object PersonCreated {
  implicit val format: Format[PersonCreated] = Json.format
}
object PersonUpdated {
  implicit val format: Format[PersonUpdated] = Json.format
}
object PersonDeactivated {
  implicit val format: Format[PersonDeactivated] = Json.format
}
object PersonActivated {
  implicit val format: Format[PersonActivated] = Json.format
}
