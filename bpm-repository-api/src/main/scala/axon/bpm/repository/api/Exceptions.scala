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

import annette.shared.exceptions.{AnnetteException, AnnetteTransportException}
import com.lightbend.lagom.scaladsl.api.transport.TransportErrorCode

object SchemaNotFound {
  val ErrorCode = TransportErrorCode.NotFound
  val MessageCode = "bpmRepository.schema.notFound"

  def apply(id: String) = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode, Map("id" -> id))
  )

}

object SchemaAlreadyExist {
  val ErrorCode = TransportErrorCode.BadRequest
  val MessageCode = "bpmRepository.schema.alreadyExist"

  def apply(id: String) = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode, Map("id" -> id))
  )

}

object XmlParseError {
  val ErrorCode = TransportErrorCode.BadRequest
  val MessageCode = "bpmRepository.schema.xmlParseError"

  def apply() = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode)
  )
}

object InvalidNotation {
  val ErrorCode = TransportErrorCode.BadRequest
  val MessageCode = "bpmRepository.schema.invalidNotation"

  def apply() = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode)
  )

}
object NotationChangeProhibited {
  val ErrorCode = TransportErrorCode.BadRequest
  val MessageCode = "bpmRepository.schema.notationChangeProhibited"

  def apply() = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode)
  )

}
