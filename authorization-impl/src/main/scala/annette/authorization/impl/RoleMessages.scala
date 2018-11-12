package annette.authorization.impl

import akka.Done
import annette.authorization.api.{BaseRole, RoleId}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import play.api.libs.json.{Format, Json}

sealed trait RoleCommand

case class CreateRole(role: BaseRole) extends RoleCommand with ReplyType[BaseRole]
case class UpdateRole(role: BaseRole) extends RoleCommand with ReplyType[BaseRole]
case class DeleteRole(id: RoleId) extends RoleCommand with ReplyType[Done]
case class FindRoleById(id: RoleId) extends RoleCommand with ReplyType[Option[BaseRole]]

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

case class RoleCreated(role: BaseRole) extends RoleEvent
case class RoleUpdated(role: BaseRole) extends RoleEvent
case class RoleDeleted(id: RoleId) extends RoleEvent
case class RoleItemsDeleted(id: RoleId) extends RoleEvent

object RoleCreated {
  implicit val format: Format[RoleCreated] = Json.format
}
object RoleUpdated {
  implicit val format: Format[RoleUpdated] = Json.format
}
object RoleDeleted {
  implicit val format: Format[RoleDeleted] = Json.format
}

object RoleItemsDeleted {
  implicit val format: Format[RoleItemsDeleted] = Json.format
}
