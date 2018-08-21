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

import com.lightbend.lagom.scaladsl.api.transport.{ExceptionMessage, TransportErrorCode, TransportException}
import play.api.libs.json.Json

class AnnetteException(val code: String, val params: Map[String, String] = Map.empty) extends RuntimeException(code) {
  def this(code: String, details: String) = {
    this(code, Json.parse(Json.parse(details).as[String]).as[Map[String, String]])
  }

  def toDetails = Json.toJson(Json.toJson(params).toString()).toString()

}

final class AnnetteTransportException(errorCode: TransportErrorCode, annetteException: AnnetteException)
    extends TransportException(errorCode, new ExceptionMessage(annetteException.code, annetteException.toDetails), annetteException) {}

object SchemaNotFound {
  val ErrorCode = TransportErrorCode.NotFound

  def apply(id: String) = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException("bpmRepository.schema.notFound", Map("id" -> id))
  )

}

object SchemaAlreadyExist {
  val ErrorCode = TransportErrorCode.BadRequest

  def apply(id: String) = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException("bpmRepository.schema.alreadyExist", Map("id" -> id))
  )

}
