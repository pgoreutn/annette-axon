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

package annette.person.repository.test

import java.time.OffsetDateTime
import scala.util.Random
import annette.person.repository.api.model.{ContactPerson, UserPerson}

object TestData {
  def userPerson(
      id: String = s"id-${Random.nextInt(9999).toString}",
      lastname: String = s"lastname-${Random.nextInt(9999).toString}",
      firstname: String = s"firstname-${Random.nextInt(9999).toString}",
      middlename: Option[String] = Some(s"middlename-${Random.nextInt(9999).toString}"),
      userId: String = s"userId-${Random.nextInt(9999).toString}",
      phone: Option[String] = Some(s"+${Random.nextInt(9999).toString}${Random.nextInt(9999999).toString}"),
      email: Option[String] = Some(s"email${Random.nextInt(99).toString}@site-${Random.nextInt(99).toString}.com")
  ): UserPerson = UserPerson(id, lastname, firstname, middlename, source = Some("keycloak"), userId, phone, email, updatedAt = OffsetDateTime.now)

  def contactPerson(
                     id: String = s"id-${Random.nextInt(9999).toString}",
                     lastname: String = s"lastname-${Random.nextInt(9999).toString}",
                     firstname: String = s"firstname-${Random.nextInt(9999).toString}",
                     middlename: Option[String] = Some(s"middlename-${Random.nextInt(9999).toString}"),
                     phone: Option[String] = Some(s"+${Random.nextInt(9999).toString}${Random.nextInt(9999999).toString}"),
                     email: Option[String] = Some(s"email${Random.nextInt(99).toString}@site-${Random.nextInt(99).toString}.com")
  ): ContactPerson = ContactPerson(id, lastname, firstname, middlename, source = Some("keycloak"), phone, email, updatedAt = OffsetDateTime.now)

}
