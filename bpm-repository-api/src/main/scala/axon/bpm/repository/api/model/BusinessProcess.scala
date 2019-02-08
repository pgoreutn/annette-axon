package axon.bpm.repository.api.model

import axon.knowledge.repository.api.model.{DataSchemaKey, DataValue}
import play.api.libs.json._

case class BusinessProcess(
    id: BusinessProcessId,
    name: String,
    description: Option[String],
    processReference: ProcessReference,
    dataSchemaKey: DataSchemaKey,
    defaults: Map[String, DataValue]
)

object BusinessProcess {
  implicit val format: Format[BusinessProcess] = Json.format
}

case class BusinessProcessSummary(
    id: BusinessProcessId,
    name: String,
    description: Option[String],
    processReference: ProcessReference,
    dataSchemaKey: DataSchemaKey,
)

object BusinessProcessSummary {
  implicit val format: Format[BusinessProcessSummary] = Json.format
}

trait ProcessReference
case class ProcessReferenceByKey(key: String) extends ProcessReference
case class ProcessReferenceById(id: String) extends ProcessReference

object ProcessReferenceByKey {
  implicit val format: Format[ProcessReferenceByKey] = Json.format
}

object ProcessReferenceById {
  implicit val format: Format[ProcessReferenceById] = Json.format
}

object ProcessReference {
  implicit val format: Format[ProcessReference] = new Format[ProcessReference] {
    def reads(json: JsValue): JsResult[ProcessReference] = {
      json match {
        case data: JsObject if data.keys.contains("reference") =>
          val reference = data.value("reference")
          reference match {
            case JsString("byKey") => Json.fromJson[ProcessReferenceByKey](data)(ProcessReferenceByKey.format)
            case JsString("byId")  => Json.fromJson[ProcessReferenceById](data)(ProcessReferenceById.format)
            case _                 => JsError(s"Unknown class '$reference'")
          }
        case _ => JsError(s"Unexpected JSON value $json")
      }
    }

    def writes(processReference: ProcessReference): JsValue = {
      processReference match {
        case b: ProcessReferenceByKey =>
          Json.toJson(b)(ProcessReferenceByKey.format).asInstanceOf[JsObject] + ("reference" -> JsString("byKey"))
        case b: ProcessReferenceById =>
          Json.toJson(b)(ProcessReferenceById.format).asInstanceOf[JsObject] + ("reference" -> JsString("byId"))
      }
    }
  }
}
