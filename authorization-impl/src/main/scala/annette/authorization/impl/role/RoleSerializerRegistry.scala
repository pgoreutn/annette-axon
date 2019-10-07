package annette.authorization.impl.role

import annette.authorization.api.model.{CheckPermissions, FindPermissions, Permission, Role, RoleFilter, RoleFindResult}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

import scala.collection.immutable

object RoleSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] = List(
    JsonSerializer[Permission],
    JsonSerializer[Role],
    JsonSerializer[CheckPermissions],
    JsonSerializer[FindPermissions],
    JsonSerializer[CreateRole],
    JsonSerializer[UpdateRole],
    JsonSerializer[DeleteRole],
    JsonSerializer[RoleCreated],
    JsonSerializer[RoleUpdated],
    JsonSerializer[RoleDeleted],
    JsonSerializer[RolePermissionDeleted],
    JsonSerializer[RoleFilter],
    JsonSerializer[RoleFindResult]
  )
}
