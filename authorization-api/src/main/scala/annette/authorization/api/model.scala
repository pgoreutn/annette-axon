package annette.authorization.api

import play.api.libs.json
import play.api.libs.json._

import scala.collection._

case class Permission(
    id: PermissionId,
    arg1: String = "",
    arg2: String = "",
    arg3: String = ""
)

object Permission {
  implicit val format: Format[Permission] = Json.format
}

class BaseRole(
    val id: RoleId,
    val name: String,
    val description: Option[String],
    val isComposite: Boolean
)

case class SimpleRole(
    override val id: RoleId,
    override val name: String,
    override val description: Option[String],
    permissions: Set[Permission]
) extends BaseRole(id, name, description, false)

object SimpleRole {
  implicit val format: Format[SimpleRole] = Json.format
}

case class CompositeRole(
    override val id: RoleId,
    override val name: String,
    override val description: Option[String],
    roles: Set[RoleId]
) extends BaseRole(id, name, description, true)

object CompositeRole {
  implicit val format: Format[CompositeRole] = Json.format
}

object BaseRole {
  implicit val format: Format[BaseRole] = new Format[BaseRole] {
    def reads(json: JsValue): JsResult[BaseRole] = {
      val isComposite = (JsPath \ "isComposite").read[Boolean].reads(json).get
      val id = (JsPath \ "id").read[String].reads(json).get
      val name = (JsPath \ "name").read[String].reads(json).get
      val description = (JsPath \ "description").readNullable[String].reads(json).get
      isComposite match {
        case false =>
          val permissions = (JsPath \ "permissions").read[Set[Permission]].reads(json).get
          JsSuccess(SimpleRole(id, name, description, permissions))
        case true =>
          val roles = (JsPath \ "roles").read[Set[RoleId]].reads(json).get
          JsSuccess(CompositeRole(id, name, description, roles))
      }

    }

    def writes(baseRole: BaseRole): JsValue = {
      implicit val f1 = SimpleRole.format
      implicit val f2 = CompositeRole.format

      val sub = baseRole match {
        case b: SimpleRole    => SimpleRole.format.writes(b)
        case b: CompositeRole => CompositeRole.format.writes(b)
      }
      JsObject(Seq("isComposite" -> json.JsBoolean(baseRole.isComposite))).deepMerge(sub.as[JsObject])
    }
  }

}

case class RoleSummary(
    id: RoleId,
    name: String,
    description: Option[String],
    isComposite: Boolean
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
