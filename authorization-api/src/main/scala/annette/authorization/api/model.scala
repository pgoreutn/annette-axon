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

package annette.authorization.api

import play.api.libs.json.{Format, Json}
import scala.collection._

case class Permission(
    id: PermissionId,
    arg1: String = "",
    arg2: String = "",
    arg3: String = ""
) {
  override def toString: UserId = {
    val argSeq = Seq(arg1, arg2, arg3).filter(!_.isEmpty)
    val args = if (argSeq.isEmpty) "" else argSeq.mkString("[", ", ", "]")
    s"$id$args"
  }
}

object Permission {
  implicit val format: Format[Permission] = Json.format
}

case class Role(
    id: RoleId,
    name: String,
    description: Option[String],
    permissions: Set[Permission]
)

object Role {
  implicit val format: Format[Role] = Json.format
}

case class RoleSummary(
    id: RoleId,
    name: String,
    description: Option[String],
)

object RoleSummary {
  implicit val format: Format[RoleSummary] = Json.format
}

case class CheckPermissions(
    roles: immutable.Set[RoleId],
    permissions: immutable.Set[Permission]
)

object CheckPermissions {
  implicit val format: Format[CheckPermissions] = Json.format
}

case class FindPermissions(
    roles: immutable.Set[RoleId],
    permissionIds: immutable.Set[PermissionId]
)

object FindPermissions {
  implicit val format: Format[FindPermissions] = Json.format
}
