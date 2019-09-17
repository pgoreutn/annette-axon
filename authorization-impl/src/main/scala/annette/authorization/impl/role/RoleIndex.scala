package annette.authorization.impl.role

import annette.authorization.api.model.{Role, RoleId}
import play.api.libs.json.{Format, Json}

case class RoleIndex(
                      id: RoleId,
                      name: String
                    )

object RoleIndex {
  def apply(role: Role): RoleIndex = RoleIndex(role.id, role.name)
  implicit val format: Format[RoleIndex] = Json.format
}
