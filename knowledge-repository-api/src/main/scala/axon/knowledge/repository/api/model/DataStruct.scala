package axon.knowledge.repository.api.model
import play.api.libs.json.{Format, Json}

case class DataStruct(
    key: DataStructKey,
    items: Map[DataItemKey, DataItem]
)

object DataStruct {
  implicit val format: Format[DataStruct] = Json.format
}

case class DataItem(
    key: DataItemKey,
    caption: Option[String],
    data: Data
)

object DataItem {
  implicit val format: Format[DataItem] = Json.format
}
