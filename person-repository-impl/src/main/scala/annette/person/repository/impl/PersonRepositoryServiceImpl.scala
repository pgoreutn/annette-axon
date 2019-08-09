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

package annette.person.repository.impl

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.Materializer
import annette.person.repository.api.PersonRepositoryService
import annette.person.repository.api.model.{Person, PersonFindQuery, PersonFindResult, PersonId}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import play.api.libs.json.{JsArray, JsNull, JsObject}

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * Implementation of the PersonService.
  */
class PersonRepositoryServiceImpl(registry: PersistentEntityRegistry, system: ActorSystem/*, dataSchemaRepository: DataSchemaRepository*/)(
    implicit ec: ExecutionContext,
    mat: Materializer)
    extends PersonRepositoryService {
  /**
    * Import person from external source. Some business rules are ignored.
    * Method performs the following steps:
    *  1. Create or update existing person, activate if person deactivated.
    *
    * @return
    */
  override def importPerson: ServiceCall[Person, Person] = ???

  /**
    * Creates person.
    * The following business rules are applied:
    * * If person already exists and active it returns exception 400 Person already exist.
    *
    * @return
    */
  override def createPerson: ServiceCall[Person, Person] = ???

  /**
    * Updates person.
    * The following business rules are applied:
    * * If person don't exist, it returns exception 404 Person not found.
    *
    * @return
    */
  override def updatePerson: ServiceCall[Person, Person] = ???

  /**
    * Deactivates person (mark it as deleted), but person's data is still can be requested by person id and remains searchable. User id is removed from UserId index. The following business rules are applied:
    * If person don't exist, it returns exception 404 Person not found.
    *
    * @param id
    * @return
    */
  override def deactivatePerson(id: PersonId): ServiceCall[NotUsed, Done] = ???

  /**
    * Activates the person deactivated earlier.
    * The following business rules are applied:
    * * The following business rules are applied:
    * * If person don't exist, it returns exception 404 Person not found.
    *
    * @param id
    * @return
    */
  override def activatePerson(id: PersonId): ServiceCall[NotUsed, Person] = ???

  /**
    * Returns person by person id. By default it return entity from read side that could be updated with some delay.
    * Parameter readSide should be se to false to get more consistent data from persistent entity. If person don't
    * exist, it returns exception 404 Person not found.
    *
    * @param id
    * @param readSide
    * @return
    */
  override def getPersonById(id: PersonId, readSide: Boolean): ServiceCall[NotUsed, Person] = ???

  /**
    * Returns persons by person ids.
    *
    * @param readSide
    * @return
    */
  override def getPersonsByIds(readSide: Boolean): ServiceCall[collection.Set[PersonId], collection.Set[Person]] = ???

  /**
    * Search person using particular query.
    *
    * @return
    */
  override def findPersons: ServiceCall[PersonFindQuery, PersonFindResult] = ???
}
