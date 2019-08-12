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
import annette.person.repository.api.model.{ContactPerson, Person, PersonType, UserPerson}
import com.datastax.driver.core.PreparedStatement
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}

import scala.concurrent.{ExecutionContext, Future}

private[impl] class PersonEventProcessor(session: CassandraSession, readSide: CassandraReadSide, elastic: PersonElastic)(implicit ec: ExecutionContext)
    extends ReadSideProcessor[PersonEvent] {
  private var insertPersonStatement: PreparedStatement = null
  private var updatePersonStatement: PreparedStatement = null
  private var deletePersonStatement: PreparedStatement = null

  def buildHandler = {
    readSide
      .builder[PersonEvent]("personEventOffset")
      .setGlobalPrepare(createTables)
      .setPrepare(_ => prepareStatements())
      .setEventHandler[PersonCreated](e => updatePerson(e.event.person))
      .setEventHandler[PersonUpdated](e => updatePerson(e.event.person))
      .setEventHandler[PersonImported](e => updatePerson(e.event.person))
      .setEventHandler[PersonDeactivated](e => updatePerson(e.event.person))
      .setEventHandler[PersonActivated](e => updatePerson(e.event.person))
      .build
  }

  def aggregateTags = PersonEvent.Tag.allTags

  private def createTables() = {
    for {
      _ <- elastic.createPersonIndex
      _ <- session.executeCreateTable("""
          |CREATE TABLE IF NOT EXISTS persons (
          |          id text PRIMARY KEY,
          |          lastname text,
          |          firstname text,
          |          middlename text,
          |          source text,
          |          person_type text,
          |          user_id text,
          |          phone text,
          |          email text,
          |          updated_at text,
          |          active boolean
          |)
          |""".stripMargin)

    } yield Done
  }

  private def prepareStatements() =
    for {
      updatePerson <- session.prepare(
        """
          | UPDATE persons SET
          |   lastname = :lastname,
          |   firstname = :firstname,
          |   middlename = :middlename,
          |   source = :source,
          |   person_type = :person_type,
          |   user_id = :user_id,
          |   phone = :phone,
          |   email = :email,
          |   updated_at = :updated_at,
          |   active = :active
          | WHERE id = :id
          |""".stripMargin
      )
    } yield {
      updatePersonStatement = updatePerson
      Done
    }

  private def updatePerson(person: Person) = {
    val (personType, userId) = person match {
      case _: ContactPerson => (PersonType.Contact.toString, null)
      case user: UserPerson => (PersonType.User.toString, user.userId)
    }
    for {
      _ <- elastic.indexPerson(person)
    } yield {
      List(
        updatePersonStatement
          .bind()
          .setString("id", person.id)
          .setString("lastname", person.lastname)
          .setString("firstname", person.firstname)
          .setString("middlename", person.middlename.orNull)
          .setString("source", person.source.orNull)
          .setString("person_type", personType)
          .setString("user_id", userId)
          .setString("phone", person.phone.orNull)
          .setString("email", person.email.orNull)
          .setString("updated_at", person.updatedAt.toString)
          .setBool("active", person.active)
      )
    }
  }

}
