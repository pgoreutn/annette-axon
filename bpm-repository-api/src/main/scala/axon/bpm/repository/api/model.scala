/***************************************************************************************
  * Copyright (c) 2014-2017 by Valery Lobachev
  * Redistribution and use in source and binary forms, with or without
  * modification, are NOT permitted without written permission from Valery Lobachev.
  *
  * Copyright (c) 2014-2017 Валерий Лобачев
  * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
  * запрещено без письменного разрешения правообладателя.
****************************************************************************************/
package axon.bpm.repository.api

import play.api.libs.json.{Format, Json}

case class Schema(id: SchemaId, name: String, description: Option[String], notation: String, xml: String, processDefinitions: Option[String])

object Schema {
  implicit val format: Format[Schema] = Json.format
}

case class SchemaSummary(id: SchemaId, name: String, description: Option[String], notation: String, processDefinitions: Option[String])

object SchemaSummary {
  implicit val format: Format[SchemaSummary] = Json.format
}

case class BusinessProcess(id: BusinessProcessId, name: String, description: Option[String], schemaKey: SchemaId)

object BusinessProcess {
  implicit val format: Format[BusinessProcess] = Json.format
}
