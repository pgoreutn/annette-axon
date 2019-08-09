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

import java.time.OffsetDateTime

import akka.Done
import annette.person.repository.api.{PersonAlreadyExist, PersonNotFound}
import annette.person.repository.api.model.{ContactPerson, Person, UserPerson}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

class PersonEntity extends PersistentEntity {
  override type Command = PersonCommand
  override type Event = PersonEvent
  override type State = Option[Person]
  override def initialState: State = None

  override def behavior: Behavior = {
    case Some(_) =>
      Actions()
        .onReadOnlyCommand[GetPersonById, Option[Person]] {
          case (GetPersonById(id), ctx, state) =>
            println
            println
            println
            println(s"GetPersonById: $id, $state")
            println
            println
            println
            ctx.reply(state)
        }
        .onReadOnlyCommand[CreatePerson, Person] {
          case (CreatePerson(person), ctx, _) =>
            ctx.commandFailed(PersonAlreadyExist(person.id))
        }
        .onCommand[UpdatePerson, Person] {
          case (UpdatePerson(person), ctx, _) =>
            val updatedPerson = updatePerson(person)
            ctx.thenPersist(PersonUpdated(updatedPerson))(_ => ctx.reply(updatedPerson))
        }
        .onCommand[ImportPerson, Person] {
          case (ImportPerson(person), ctx, _) =>
            val importedPerson = updatePerson(person)
            ctx.thenPersist(PersonImported(importedPerson))(_ => ctx.reply(importedPerson))
        }
        .onCommand[DeactivatePerson, Done] {
          case (DeactivatePerson(_), ctx, Some(person)) =>
            val deactivatedPerson = updatePerson(person, Some(false))
            ctx.thenPersist(PersonDeactivated(deactivatedPerson))(_ => ctx.reply(Done))
        }
        .onCommand[ActivatePerson, Person] {
          case (ActivatePerson(_), ctx, Some(person)) =>
            val activatedPerson = updatePerson(person, Some(true))
            ctx.thenPersist(PersonActivated(activatedPerson))(_ => ctx.reply(activatedPerson))
        }
        .onEvent {
          case (PersonUpdated(person), _) =>
            Some(person)
          case (PersonImported(person), _) =>
            Some(person)
          case (PersonDeactivated(person), _) =>
            Some(person)
          case (PersonActivated(person), _) =>
            Some(person)
        }

    case None =>
      Actions()
        .onCommand[CreatePerson, Person] {
          case (CreatePerson(person), ctx, _) =>
            println
            println
            println
            println(s"Create Person: $person")
            println
            println
            println
            val createdPerson = updatePerson(person)
            ctx.thenPersist(PersonCreated(createdPerson))(_ => ctx.reply(createdPerson))
        }
        .onCommand[ImportPerson, Person] {
          case (ImportPerson(person), ctx, _) =>
            val importedPerson = updatePerson(person)
            ctx.thenPersist(PersonImported(importedPerson))(_ => ctx.reply(importedPerson))
        }
        .onReadOnlyCommand[UpdatePerson, Person] {
          case (UpdatePerson(person), ctx, _) => ctx.commandFailed(PersonNotFound(person.id))
        }
        .onReadOnlyCommand[DeactivatePerson, Done] {
          case (DeactivatePerson(id), ctx, _) => ctx.commandFailed(PersonNotFound(id))
        }
        .onReadOnlyCommand[ActivatePerson, Person] {
          case (ActivatePerson(id), ctx, _) => ctx.commandFailed(PersonNotFound(id))
        }
        .onReadOnlyCommand[GetPersonById, Option[Person]] {
          case (GetPersonById(id), ctx, state) =>
            println
            println
            println
            println(s"GetPersonById: $id, $state")
            println
            println
            println
            ctx.reply(None)
        }
        .onEvent {
          case (PersonCreated(person), _) =>
            Some(person)
          case (PersonImported(person), _) =>
            Some(person)
        }
  }

  def updatePerson(person: Person, maybeActive: Option[Boolean] = None): Person = {
    person match {
      case contact: ContactPerson => contact.copy(updatedAt = OffsetDateTime.now(), active = maybeActive.getOrElse(person.active))
      case user: UserPerson       => user.copy(updatedAt = OffsetDateTime.now(), active = maybeActive.getOrElse(person.active))
    }
  }

}
