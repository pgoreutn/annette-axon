package axon.knowledge.repository.impl.schema

import akka.Done
import axon.knowledge.repository.api.model.{DataSchema, DataSchemaKey}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import play.api.libs.json.{Format, Json}

sealed trait DataSchemaCommand

case class CreateDataSchema(dataSchema: DataSchema) extends DataSchemaCommand with ReplyType[DataSchema]
case class UpdateDataSchema(dataSchema: DataSchema) extends DataSchemaCommand with ReplyType[DataSchema]
case class DeleteDataSchema(key: DataSchemaKey) extends DataSchemaCommand with ReplyType[Done]
case class FindDataSchemaByKey(key: DataSchemaKey) extends DataSchemaCommand with ReplyType[Option[DataSchema]]

object CreateDataSchema {
  implicit val format: Format[CreateDataSchema] = Json.format
}
object UpdateDataSchema {
  implicit val format: Format[UpdateDataSchema] = Json.format
}
object DeleteDataSchema {
  implicit val format: Format[DeleteDataSchema] = Json.format
}
object FindDataSchemaByKey {
  implicit val format: Format[FindDataSchemaByKey] = Json.format
}

sealed trait DataSchemaEvent extends AggregateEvent[DataSchemaEvent] {
  override def aggregateTag = DataSchemaEvent.Tag
}

object DataSchemaEvent {
  val NumShards = 4
  val Tag = AggregateEventTag.sharded[DataSchemaEvent](NumShards)
}

case class DataSchemaCreated(dataSchema: DataSchema) extends DataSchemaEvent
case class DataSchemaUpdated(dataSchema: DataSchema) extends DataSchemaEvent
case class DataSchemaDeleted(key: DataSchemaKey) extends DataSchemaEvent

object DataSchemaCreated {
  implicit val format: Format[DataSchemaCreated] = Json.format
}
object DataSchemaUpdated {
  implicit val format: Format[DataSchemaUpdated] = Json.format
}
object DataSchemaDeleted {
  implicit val format: Format[DataSchemaDeleted] = Json.format
}
