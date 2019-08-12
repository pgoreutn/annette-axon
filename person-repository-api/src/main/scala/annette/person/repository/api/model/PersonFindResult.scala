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

import play.api.libs.json.{Format, Json}

case class PersonFindResult(
    total: Long, // total items in query
    hits: Seq[PersonHitResult] // results of search
)

object PersonFindResult {
  implicit val format: Format[PersonFindResult] = Json.format
}

case class PersonHitResult(
    id: PersonId, // person id
    score: Float, // store of this hit
    updatedAt: OffsetDateTime // date/time of last update
)
object PersonHitResult {
  implicit val format: Format[PersonHitResult] = Json.format
}
