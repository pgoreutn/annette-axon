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
import play.api.libs.json.{Json, JsonConfiguration, JsonNaming, OFormat}
import annette.security.auth.UserId

sealed trait Person {
  val id: PersonId // person id
  val lastname: String // last name of the person
  val firstname: String // first name
  val middlename: Option[String] // middle name
  val source: Option[String] // source of person data
  val phone: Option[String] // phone
  val email: Option[String] // email
  val updatedAt: OffsetDateTime // date/time of last update
  val active: Boolean // marks person entity as active (not deleted)
}

case class UserPerson(
    id: PersonId, // person id
    lastname: String, // last name of the person
    firstname: String, // first name
    middlename: Option[String] = None, // middle name
    source: Option[String] = None, // source of person data
    userId: UserId, // user id of person
    phone: Option[String] = None, // phone
    email: Option[String] = None, // email
    updatedAt: OffsetDateTime, // date/time of last update
    active: Boolean = true // marks person entity as active (not deleted)
) extends Person

object UserPerson {
  implicit val format = Json.format[UserPerson]
}

case class ContactPerson(
    id: PersonId, // person id
    lastname: String, // last name of the person
    firstname: String, // first name
    middlename: Option[String] = None, // middle name
    source: Option[String] = None, // source of person data
    phone: Option[String] = None, // phone
    email: Option[String] = None, // email
    updatedAt: OffsetDateTime, // date/time of last update
    active: Boolean = true // marks person entity as active (not deleted)
) extends Person

object ContactPerson {
  implicit val format = Json.format[ContactPerson]
}

object Person {
  implicit val config = JsonConfiguration(discriminator = "personType", typeNaming = JsonNaming { fullName =>
    fullName.split("\\.").toSeq.last
  })

  implicit val format: OFormat[Person] = Json.format[Person]
}
