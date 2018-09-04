package axon.bpm.repository.impl
import akka.Done
import axon.bpm.repository.api.{Schema, SchemaId}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import play.api.libs.json.{Format, Json}

sealed trait SchemaCommand

case class CreateSchema(id: SchemaId, name: String, description: Option[String], notation: String, schema: String)
    extends SchemaCommand
    with ReplyType[Done]
case class UpdateSchema(id: SchemaId, name: String, description: Option[String], schema: String) extends SchemaCommand with ReplyType[Done]
case class DeleteSchema(id: SchemaId) extends SchemaCommand with ReplyType[Done]
case class FindSchemaById(id: SchemaId) extends SchemaCommand with ReplyType[Option[Schema]]

object CreateSchema {
  implicit val format: Format[CreateSchema] = Json.format
}
object UpdateSchema {
  implicit val format: Format[UpdateSchema] = Json.format
}
object DeleteSchema {
  implicit val format: Format[DeleteSchema] = Json.format
}
object FindSchemaById {
  implicit val format: Format[FindSchemaById] = Json.format
}

sealed trait SchemaEvent extends AggregateEvent[SchemaEvent] {
  override def aggregateTag = SchemaEvent.Tag
}

object SchemaEvent {
  val NumShards = 4
  val Tag = AggregateEventTag.sharded[SchemaEvent](NumShards)
}

case class SchemaCreated(id: SchemaId, name: String, description: Option[String], notation: String, schema: String) extends SchemaEvent
case class SchemaUpdated(id: SchemaId, name: String, description: Option[String], schema: String) extends SchemaEvent
case class SchemaDeleted(id: SchemaId) extends SchemaEvent

object SchemaCreated {
  implicit val format: Format[SchemaCreated] = Json.format
}
object SchemaUpdated {
  implicit val format: Format[SchemaUpdated] = Json.format
}
object SchemaDeleted {
  implicit val format: Format[SchemaDeleted] = Json.format
}
