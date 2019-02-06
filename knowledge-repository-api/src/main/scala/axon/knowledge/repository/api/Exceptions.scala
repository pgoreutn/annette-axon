/***************************************************************************************
  * Copyright (c) 2014-2017 by Valery Lobachev
  * Redistribution and use in source and binary forms, with or without
  * modification, are NOT permitted without written permission from Valery Lobachev.
  *
  * Copyright (c) 2014-2017 Валерий Лобачев
  * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
  * запрещено без письменного разрешения правообладателя.
****************************************************************************************/
package axon.knowledge.repository.api

import annette.shared.exceptions.{AnnetteException, AnnetteTransportException}
import com.lightbend.lagom.scaladsl.api.transport.TransportErrorCode

object DataSchemaNotFound {
  val ErrorCode = TransportErrorCode.NotFound
  val MessageCode = "knowledgeRepository.dataSchema.notFound"

  def apply(key: String) = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode, Map("id" -> key), Some(s"$MessageCode($key)"))
  )
}

object DataSchemasNotExist {
  val ErrorCode = TransportErrorCode.NotFound
  val MessageCode = "knowledgeRepository.dataSchema.notFound"

  def apply(keys: Seq[String]) = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode, Map("id" -> keys.mkString(", ")), Some(s"$MessageCode($keys)"))
  )

}

object DataSchemaAlreadyExist {
  val ErrorCode = TransportErrorCode.BadRequest
  val MessageCode = "knowledgeRepository.dataSchema.alreadyExist"

  def apply(id: String) = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode, Map("id" -> id), Some(s"$MessageCode($id)"))
  )

}

object KeyRequired {
  val ErrorCode = TransportErrorCode.BadRequest
  val MessageCode = "knowledgeRepository.dataSchema.keyRequired"

  def apply() = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode)
  )

}
