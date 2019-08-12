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

import annette.person.repository.api.model.PersonType.PersonType
import annette.person.repository.api.model.{ContactPerson, Person, PersonId, PersonType, UserPerson}
import play.api.libs.json.{Format, Json}

case class PersonIndex(
    id: PersonId,
    lastname: String,
    firstname: String,
    middlename: Option[String],
    personType: PersonType,
    phone: Option[String],
    email: Option[String],
    updatedAt: OffsetDateTime,
    active: Boolean
)

object PersonIndex {
  implicit val format: Format[PersonIndex] = Json.format

  def apply(person: Person): PersonIndex = {
    val personType: PersonType = person match {
      case _: UserPerson    => PersonType.User
      case _: ContactPerson => PersonType.Contact
    }
    PersonIndex(
      id = person.id,
      lastname = person.lastname,
      firstname = person.firstname,
      middlename = person.middlename,
      personType = personType,
      phone = person.phone,
      email = person.email,
      updatedAt = person.updatedAt,
      active = person.active
    )
  }
}
