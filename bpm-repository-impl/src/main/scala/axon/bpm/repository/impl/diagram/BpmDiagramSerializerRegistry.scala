package axon.bpm.repository.impl.bpmDiagram

import axon.bpm.repository.api.BpmDiagram
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

object BpmDiagramSerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(
    JsonSerializer[BpmDiagram],
    JsonSerializer[CreateBpmDiagram],
    JsonSerializer[UpdateBpmDiagram],
    JsonSerializer[DeleteBpmDiagram],
    JsonSerializer[FindBpmDiagramById],
    JsonSerializer[BpmDiagramCreated],
    JsonSerializer[BpmDiagramUpdated],
    JsonSerializer[BpmDiagramDeleted],
  )
}
