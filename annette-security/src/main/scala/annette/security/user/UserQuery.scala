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

package annette.security.user
import play.api.libs.json.{Format, Json}

case class UserQuery(
    search: Option[String] = None, // A String contained in username, first or last name, or email
    firstName: Option[String] = None,
    lastName: Option[String] = None,
    email: Option[String] = None,
    username: Option[String] = None,
    briefRepresentation: Option[Boolean] = Some(false),
    max: Option[Int] = Some(100),
    first: Option[Int] = None
)

object UserQuery {
  implicit val format: Format[UserQuery] = Json.format
}
