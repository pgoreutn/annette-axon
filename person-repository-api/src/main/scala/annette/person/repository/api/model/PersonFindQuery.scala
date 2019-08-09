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

import annette.person.repository.api.model.PersonSortBy.PersonSortBy
import annette.person.repository.api.model.PersonType.PersonType
import play.api.libs.json.{Format, Json}


object PersonSortBy extends Enumeration {
  type PersonSortBy = Value

  val Lastname = Value("lastname") // Sort by last name, first name, middle name
  val Firstname = Value("firstname") // sort by first name, last name
  val Phone = Value("phone") // sort by phone
  val Email = Value("email") //sort by email

  implicit val format = Json.formatEnum(this)
}

object PersonType extends Enumeration {
  type PersonType = Value

  val User = Value("user")
  val Contact = Value("contact")

  implicit val format = Json.formatEnum(this)
}

case class PersonFindQuery(
    offset: Int = 0,
    size: Int,
    filter: Option[String] = None, //search by filter in person's names, email and phone
    lastname: Option[String] = None, //search in last name of the person
    firstname: Option[String] = None, //search in first name
    middlename: Option[String] = None, //search in middle name
    personType: Option[PersonType] = None, //search in type of person: user or contact
    phone: Option[String] = None, //search in phone
    email: Option[String] = None, //search in email
    activeOnly: Boolean = true, //search active persons only (by default)
    sortBy: Option[PersonSortBy] = Some(PersonSortBy.Lastname), //sort results by field provided
    ascending: Boolean = true
)

object PersonFindQuery {
  implicit val format: Format[PersonFindQuery] = Json.format
}
