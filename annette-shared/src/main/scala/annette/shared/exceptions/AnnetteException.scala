/***************************************************************************************
  * Copyright (c) 2014-2017 by Valery Lobachev
  * Redistribution and use in source and binary forms, with or without
  * modification, are NOT permitted without written permission from Valery Lobachev.
  *
  * Copyright (c) 2014-2017 Валерий Лобачев
  * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
  * запрещено без письменного разрешения правообладателя.
****************************************************************************************/
package annette.shared.exceptions

import play.api.libs.json.Json

class AnnetteException(val code: String, val params: Map[String, String] = Map.empty) extends RuntimeException(code) {
  def this(code: String, details: String) = {
    this(code, Json.parse(Json.parse(details).as[String]).as[Map[String, String]])
  }

  def toDetails = Json.toJson(Json.toJson(params).toString()).toString()
  def toMessage = Json.toJson(params + ("code" -> code))

}
