package axon.bpm.repository.impl.bpmDiagram

import akka.Done
import axon.bpm.repository.api.{BpmDiagram, BpmDiagramId}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import play.api.libs.json.{Format, Json}

sealed trait BpmDiagramCommand

case class CreateBpmDiagram(bpmDiagram: BpmDiagram) extends BpmDiagramCommand with ReplyType[BpmDiagram]
case class UpdateBpmDiagram(bpmDiagram: BpmDiagram) extends BpmDiagramCommand with ReplyType[BpmDiagram]
case class DeleteBpmDiagram(id: BpmDiagramId) extends BpmDiagramCommand with ReplyType[Done]
case class FindBpmDiagramById(id: BpmDiagramId) extends BpmDiagramCommand with ReplyType[Option[BpmDiagram]]

object CreateBpmDiagram {
  implicit val format: Format[CreateBpmDiagram] = Json.format
}
object UpdateBpmDiagram {
  implicit val format: Format[UpdateBpmDiagram] = Json.format
}
object DeleteBpmDiagram {
  implicit val format: Format[DeleteBpmDiagram] = Json.format
}
object FindBpmDiagramById {
  implicit val format: Format[FindBpmDiagramById] = Json.format
}

sealed trait BpmDiagramEvent extends AggregateEvent[BpmDiagramEvent] {
  override def aggregateTag = BpmDiagramEvent.Tag
}

object BpmDiagramEvent {
  val NumShards = 4
  val Tag = AggregateEventTag.sharded[BpmDiagramEvent](NumShards)
}

case class BpmDiagramCreated(bpmDiagram: BpmDiagram) extends BpmDiagramEvent
case class BpmDiagramUpdated(bpmDiagram: BpmDiagram) extends BpmDiagramEvent
case class BpmDiagramDeleted(id: BpmDiagramId) extends BpmDiagramEvent

object BpmDiagramCreated {
  implicit val format: Format[BpmDiagramCreated] = Json.format
}
object BpmDiagramUpdated {
  implicit val format: Format[BpmDiagramUpdated] = Json.format
}
object BpmDiagramDeleted {
  implicit val format: Format[BpmDiagramDeleted] = Json.format
}
