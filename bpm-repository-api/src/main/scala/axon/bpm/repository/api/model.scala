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

case class BpmDiagram(id: BpmDiagramId, name: String, description: Option[String], notation: String, xml: String, processDefinitions: Option[String] = None)

object BpmDiagram {
  implicit val format: Format[BpmDiagram] = Json.format
}

case class BpmDiagramSummary(id: BpmDiagramId, name: String, description: Option[String], notation: String, processDefinitions: Option[String])

object BpmDiagramSummary {
  implicit val format: Format[BpmDiagramSummary] = Json.format
}

case class BusinessProcess(id: BusinessProcessId, name: String, description: Option[String], bpmDiagramKey: BpmDiagramId)

object BusinessProcess {
  implicit val format: Format[BusinessProcess] = Json.format
}
