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
import play.api.libs.json.{Format, Json}

case class DataStructDef(
    key: DataStructKey,
    name: String,
    description: Option[String],
    baseObjects: Seq[DataStructKey],
    items: Map[DataItemKey, DataItemDef]
)

object DataStructDef {
  implicit val format: Format[DataStructDef] = Json.format
}

case class DataItemDef(
    key: DataItemKey,
    name: String,
    description: Option[String],
    caption: String,
    data: Data
)

object DataItemDef {
  implicit val format: Format[DataItemDef] = Json.format
}
