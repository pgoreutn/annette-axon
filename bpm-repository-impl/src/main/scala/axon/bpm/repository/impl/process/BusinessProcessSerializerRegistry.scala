package axon.bpm.repository.impl.process
import axon.bpm.repository.api.model.BusinessProcess
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

object BusinessProcessSerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(
    JsonSerializer[BusinessProcess],
    JsonSerializer[CreateBusinessProcess],
    JsonSerializer[UpdateBusinessProcess],
    JsonSerializer[DeleteBusinessProcess],
    JsonSerializer[FindBusinessProcessById],
    JsonSerializer[BusinessProcessCreated],
    JsonSerializer[BusinessProcessUpdated],
    JsonSerializer[BusinessProcessDeleted],
  )
}
