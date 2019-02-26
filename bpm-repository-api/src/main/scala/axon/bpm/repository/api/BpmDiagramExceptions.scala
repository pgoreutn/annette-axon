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

object BpmDiagramNotFound {
  val ErrorCode = TransportErrorCode.NotFound
  val MessageCode = "bpmRepository.bpmDiagram.notFound"

  def apply(id: String) = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode, Map("id" -> id))
  )

}

object BpmDiagramAlreadyExist {
  val ErrorCode = TransportErrorCode.BadRequest
  val MessageCode = "bpmRepository.bpmDiagram.alreadyExist"

  def apply(id: String) = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode, Map("id" -> id))
  )

}

object BpmDiagramIdRequired {
  val ErrorCode = TransportErrorCode.BadRequest
  val MessageCode = "bpmRepository.bpmDiagram.idRequired"

  def apply() = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode)
  )

}

object InvalidNotation {
  val ErrorCode = TransportErrorCode.BadRequest
  val MessageCode = "bpmRepository.bpmDiagram.invalidNotation"

  def apply() = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode)
  )
}

object NotationChangeProhibited {
  val ErrorCode = TransportErrorCode.BadRequest
  val MessageCode = "bpmRepository.bpmDiagram.notationChangeProhibited"

  def apply() = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode)
  )

}