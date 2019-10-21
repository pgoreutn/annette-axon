package annette.orgstructure.impl.orgrole

import java.time.OffsetDateTime

import annette.orgstructure.api.model.{OrgRole, OrgRoleId}
import play.api.libs.json.{Format, Json}

case class OrgRoleIndex(
    id: OrgRoleId,
    name: String,
    updatedAt: OffsetDateTime,
    active: Boolean
)

object OrgRoleIndex {
  def apply(orgRole: OrgRole): OrgRoleIndex = OrgRoleIndex(orgRole.id, orgRole.name, orgRole.updatedAt, orgRole.active)
  implicit val format: Format[OrgRoleIndex] = Json.format
}
