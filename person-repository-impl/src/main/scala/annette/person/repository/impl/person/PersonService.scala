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
import akka.actor.ActorSystem
import akka.stream.Materializer
import annette.person.repository.api.PersonNotFound
import annette.person.repository.api.model.{Person, PersonFindQuery, PersonFindResult, PersonId}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.{ExecutionContext, Future}

class PersonService(registry: PersistentEntityRegistry, system: ActorSystem, personRepository: PersonRepository)(
    implicit ec: ExecutionContext,
    mat: Materializer
) {

  def importPerson(person: Person): Future[Person] = {
    refFor(person.id).ask(ImportPerson(person))
  }

  def createPerson(person: Person): Future[Person] = {
    refFor(person.id).ask(CreatePerson(person))
  }

  def updatePerson(person: Person): Future[Person] = {
    refFor(person.id).ask(UpdatePerson(person))
  }

  def deactivatePerson(id: PersonId): Future[Done] = {
    refFor(id).ask(DeactivatePerson(id))
  }

  def activatePerson(id: PersonId): Future[Person] = {
    refFor(id).ask(ActivatePerson(id))
  }

  def getPersonById(id: PersonId, readSide: Boolean = true): Future[Person] = {
    for {
      maybePerson <- if (readSide) {
        personRepository.getPersonById(id)
      } else {
        refFor(id).ask(GetPersonById(id))
      }
    } yield {
      maybePerson match {
        case Some(person) => person
        case None         => throw PersonNotFound(id)
      }
    }
  }

  def getPersonsByIds(ids: Set[PersonId], readSide: Boolean = true): Future[Set[Person]] = {
    if (readSide) {
      personRepository.getPersonsByIds(ids)
    } else {
      Future
        .traverse(ids)(id => refFor(id).ask(GetPersonById(id)))
        .map(seq => seq.flatten)
    }
  }

  def findPersons(query: PersonFindQuery): Future[PersonFindResult] = {
    personRepository.findPersons(query)
  }

  private def refFor(id: PersonId) = {
    registry.refFor[PersonEntity](id)

  }

}
