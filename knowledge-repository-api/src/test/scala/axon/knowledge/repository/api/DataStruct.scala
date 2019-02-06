package axon.knowledge.repository.api

import axon.knowledge.repository.api.model.{DataSchemaFieldKey, DataSchemaKey, Datatype}
import play.api.libs.json.{Format, JsValue, Json}

case class DataStruct(
    key: DataSchemaKey,
    items: Map[DataSchemaFieldKey, DataItem],
)

case class DataItem(
    key: DataSchemaFieldKey,
    datatype: Datatype,
    value: Option[JsValue],
)

object DataStruct {
  implicit val format: Format[DataStruct] = Json.format
}

object DataItem {
  implicit val format: Format[DataItem] = Json.format
}
