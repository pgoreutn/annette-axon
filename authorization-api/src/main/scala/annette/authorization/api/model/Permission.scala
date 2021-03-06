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

import scala.collection.Seq

case class Permission(
    id: PermissionId,
    arg1: String = "",
    arg2: String = "",
    arg3: String = ""
) {
  override def toString: String = {
    val argSeq = Seq(arg1, arg2, arg3).filter(!_.isEmpty)
    val args = if (argSeq.isEmpty) "" else argSeq.mkString("[", ", ", "]")
    s"$id$args"
  }
}

object Permission {
  implicit val format: Format[Permission] = Json.format
}
