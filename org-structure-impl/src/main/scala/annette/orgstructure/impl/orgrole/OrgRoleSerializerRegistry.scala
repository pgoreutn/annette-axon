package annette.orgstructure.impl.orgrole

import annette.orgstructure.api.model.{ OrgRole, OrgRoleFilter, OrgRoleFindResult}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

import scala.collection.immutable

object OrgRoleSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] = List(
    JsonSerializer[OrgRole],
    JsonSerializer[CreateOrgRole],
    JsonSerializer[UpdateOrgRole],
    JsonSerializer[DeactivateOrgRole],
    JsonSerializer[ActivateOrgRole],
    JsonSerializer[OrgRoleCreated],
    JsonSerializer[OrgRoleUpdated],
    JsonSerializer[OrgRoleDeactivated],
    JsonSerializer[OrgRoleActivated],
    JsonSerializer[OrgRoleFilter],
    JsonSerializer[OrgRoleFindResult]
  )
}
