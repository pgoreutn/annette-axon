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

package annette.authorization.api.model

import play.api.libs.json.{Format, Json}

case class RoleFilter(
    offset: Int, // смещение (начиная с 0)
    size: Int, // размер страницы

    filterByName: Option[String], // Если указано, то фильтрует список по полю name

    sortBy: Option[String] = None,
    ascending: Boolean = true
)

object RoleFilter {
  implicit val format: Format[RoleFilter] = Json.format
}
