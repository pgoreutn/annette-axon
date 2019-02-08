package axon.bpm.repository.api.model

import play.api.libs.json._

case class BpmDiagram(
    id: BpmDiagramId,
    name: String,
    description: Option[String],
    notation: String,
    xml: String,
    processDefinitions: Option[String] = None
)

object BpmDiagram {
  implicit val format: Format[BpmDiagram] = Json.format
}

case class BpmDiagramSummary(
    id: BpmDiagramId,
    name: String,
    description: Option[String],
    notation: String,
    processDefinitions: Option[String]
)

object BpmDiagramSummary {
  implicit val format: Format[BpmDiagramSummary] = Json.format
}
