package axon.knowledge.repository.impl.schema

import axon.knowledge.repository.api.model.DataSchema
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

object DataSchemaSerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(
    JsonSerializer[DataSchema],
    JsonSerializer[CreateDataSchema],
    JsonSerializer[UpdateDataSchema],
    JsonSerializer[DeleteDataSchema],
    JsonSerializer[FindDataSchemaByKey],
    JsonSerializer[DataSchemaCreated],
    JsonSerializer[DataSchemaUpdated],
    JsonSerializer[DataSchemaDeleted],
  )
}
