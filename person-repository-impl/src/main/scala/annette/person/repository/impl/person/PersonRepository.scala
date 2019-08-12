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

import annette.person.repository.api.model.{ContactPerson, Person, PersonFindQuery, PersonFindResult, PersonId, PersonType, UserPerson}
import com.datastax.driver.core.Row
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import scala.collection.JavaConverters._
import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

private[impl] class PersonRepository(session: CassandraSession, elastic: PersonElastic)(implicit ec: ExecutionContext) {
  def getPersonById(id: PersonId): Future[Option[Person]] = {
    for {
      stmt <- session.prepare("SELECT * FROM persons WHERE id = ?")
      result <- session.selectOne(stmt.bind(id)).map(_.map(convertPerson))
    } yield result
  }

  def getPersonsByIds(ids: Set[PersonId]): Future[Set[Person]] = {
    for {
      stmt <- session.prepare("SELECT * FROM persons WHERE id IN ?")
      result <- session.selectAll(stmt.bind(ids.toList.asJava)).map(_.map(convertPerson))
    } yield result.to[Set]
  }

  def findPersons(query: PersonFindQuery): Future[PersonFindResult] = {
    elastic.findPerson(query)
  }

  private def convertPerson(row: Row): Person = {
    row.getString("person_type") match {
      case "user" =>
        UserPerson(
          id = row.getString("id"),
          lastname = row.getString("lastname"),
          firstname = row.getString("firstname"),
          middlename = Option(row.getString("middlename")),
          source = Option(row.getString("source")),
          userId = row.getString("user_id"),
          phone = Option(row.getString("phone")),
          email = Option(row.getString("email")),
          updatedAt = OffsetDateTime.parse(row.getString("updated_at")),
          active = row.getBool("active")
        )
      case "contact" =>
        ContactPerson(
          id = row.getString("id"),
          lastname = row.getString("lastname"),
          firstname = row.getString("firstname"),
          middlename = Option(row.getString("middlename")),
          source = Option(row.getString("source")),
          phone = Option(row.getString("phone")),
          email = Option(row.getString("email")),
          updatedAt = OffsetDateTime.parse(row.getString("updated_at")),
          active = row.getBool("active")
        )
    }

  }
}
