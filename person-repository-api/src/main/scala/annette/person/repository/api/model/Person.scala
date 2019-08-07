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

package annette.person.repository.api.model

import java.time.OffsetDateTime

import annette.person.repository.api.model.PersonType.PersonType
import annette.security.auth.UserId
import play.api.libs.json.{Format, Json}

case class Person(
    id: PersonId, // person id
    lastname: String, // last name of the person
    firstname: String, // first name
    middlename: Option[String], // middle name
    personType: PersonType, // type of person: user or contact
    source: Option[String], // source of person data
    userId: Option[UserId], // user id of person
    phone: Option[String], // phone
    email: Option[String], // email
    updatedAt: OffsetDateTime, // date/time of last update
    active: Boolean // marks person entity as active (not deleted)
)

object Person {
  implicit val format: Format[Person] = Json.format
}

object PersonType extends Enumeration {
  type PersonType = Value

  val User = Value("user")
  val Contact = Value("contact")

  implicit val format = Json.formatEnum(this)
}
