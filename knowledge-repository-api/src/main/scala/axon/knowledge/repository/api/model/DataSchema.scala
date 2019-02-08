/***************************************************************************************
  * Copyright (c) 2014-2017 by Valery Lobachev
  * Redistribution and use in source and binary forms, with or without
  * modification, are NOT permitted without written permission from Valery Lobachev.
  *
  * Copyright (c) 2014-2017 Валерий Лобачев
  * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
  * запрещено без письменного разрешения правообладателя.
****************************************************************************************/
package axon.knowledge.repository.api.model
import play.api.libs.json.{Format, JsValue, Json}

case class DataSchemaSummary(
    key: DataSchemaKey,
    name: String,
    description: Option[String],
)

case class DataSchema(
    key: DataSchemaKey,
    name: String,
    description: Option[String],
    baseSchemas: Seq[DataSchemaKey],
    fields: Map[DataSchemaFieldKey, DataSchemaField],
)

case class DataSchemaField(
    key: DataSchemaFieldKey,
    name: String,
    caption: String,
    datatype: Datatype,
    value: Option[JsValue],
)

case class DataValue(
    key: DataSchemaFieldKey,
    datatype: Datatype,
    value: Option[JsValue],
)

object DataSchemaSummary {
  implicit val format: Format[DataSchemaSummary] = Json.format
}

object DataSchema {
  implicit val format: Format[DataSchema] = Json.format
}

object DataSchemaField {
  implicit val format: Format[DataSchemaField] = Json.format
}

object DataValue {
  implicit val format: Format[DataValue] = Json.format
}
