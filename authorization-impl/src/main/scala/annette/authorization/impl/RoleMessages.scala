package annette.authorization.impl

import akka.Done
import annette.authorization.api.{Role, RoleId}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import play.api.libs.json.{Format, Json}

sealed trait RoleCommand

case class CreateRole(role: Role) extends RoleCommand with ReplyType[Role]
case class UpdateRole(role: Role) extends RoleCommand with ReplyType[Role]
case class DeleteRole(id: RoleId) extends RoleCommand with ReplyType[Done]
case class FindRoleById(id: RoleId) extends RoleCommand with ReplyType[Option[Role]]

object CreateRole {
  implicit val format: Format[CreateRole] = Json.format
}
object UpdateRole {
  implicit val format: Format[UpdateRole] = Json.format
}
object DeleteRole {
  implicit val format: Format[DeleteRole] = Json.format
}
object FindRoleById {
  implicit val format: Format[FindRoleById] = Json.format
}

sealed trait RoleEvent extends AggregateEvent[RoleEvent] {
  override def aggregateTag = RoleEvent.Tag
}

object RoleEvent {
  val NumShards = 4
  val Tag = AggregateEventTag.sharded[RoleEvent](NumShards)
}

case class RoleCreated(role: Role) extends RoleEvent
case class RoleUpdated(role: Role) extends RoleEvent
case class RoleDeleted(id: RoleId) extends RoleEvent
case class RolePermissionDeleted(id: RoleId) extends RoleEvent

object RoleCreated {
  implicit val format: Format[RoleCreated] = Json.format
}
object RoleUpdated {
  implicit val format: Format[RoleUpdated] = Json.format
}
object RoleDeleted {
  implicit val format: Format[RoleDeleted] = Json.format
}

object RolePermissionDeleted {
  implicit val format: Format[RolePermissionDeleted] = Json.format
}
