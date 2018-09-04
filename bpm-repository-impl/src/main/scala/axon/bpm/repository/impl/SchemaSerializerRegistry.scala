package axon.bpm.repository.impl
import axon.bpm.repository.api.Schema
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

object SchemaSerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(
    JsonSerializer[Schema],
    JsonSerializer[CreateSchema],
    JsonSerializer[UpdateSchema],
    JsonSerializer[DeleteSchema],
    JsonSerializer[FindSchemaById],
    JsonSerializer[SchemaCreated],
    JsonSerializer[SchemaUpdated],
    JsonSerializer[SchemaDeleted],
  )
}
