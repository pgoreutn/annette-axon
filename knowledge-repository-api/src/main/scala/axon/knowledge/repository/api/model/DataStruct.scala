package axon.knowledge.repository.api.model
import play.api.libs.json.{Format, JsValue, Json}

case class DataStruct(
    key: DataStructKey,
    items: Map[DataItemKey, DataItem],
)

case class DataItem(
    key: DataItemKey,
    datatype: Datatype,
    value: Option[JsValue],
)

object DataStruct {
  implicit val format: Format[DataStruct] = Json.format
}

object DataItem {
  implicit val format: Format[DataItem] = Json.format
}
