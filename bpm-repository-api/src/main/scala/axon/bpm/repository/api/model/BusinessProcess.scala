/*
 * Copyright 2018 Valery Lobachev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package axon.bpm.repository.api.model

import axon.knowledge.repository.api.model.{DataSchemaKey, DataValue}
import play.api.libs.json._

case class BusinessProcess(
    key: BusinessProcessKey,
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
    key: BusinessProcessKey,
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