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

import annette.person.repository.api.model.PersonType.PersonType
import play.api.libs.json.{Format, Json}

case class PersonFindQuery(
    offset: Int,
    size: Int,
    filter: Option[String], //search by filter in person's names, email and phone
    lastname: Option[String], //search in last name of the person
    firstname: Option[String], //search in first name
    middlename: Option[String], //search in middle name
    personType: Option[PersonType], //search in type of person: user or contact
    phone: Option[String], //search in phone
    email: Option[String], //search in email
    activeOnly: Boolean, //search active persons only (by default)
    sortBy: Option[String] //sort results by field provided
)

object PersonFindQuery {
  implicit val format: Format[PersonFindQuery] = Json.format
}