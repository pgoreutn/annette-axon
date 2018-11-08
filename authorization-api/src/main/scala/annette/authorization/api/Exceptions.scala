/***************************************************************************************
  * Copyright (c) 2014-2017 by Valery Lobachev
  * Redistribution and use in source and binary forms, with or without
  * modification, are NOT permitted without written permission from Valery Lobachev.
  *
  * Copyright (c) 2014-2017 Валерий Лобачев
  * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
  * запрещено без письменного разрешения правообладателя.
****************************************************************************************/
package annette.authorization.api

import annette.shared.exceptions.{AnnetteException, AnnetteTransportException}
import com.lightbend.lagom.scaladsl.api.transport.TransportErrorCode

object RoleNotFound {
  val ErrorCode = TransportErrorCode.NotFound
  val MessageCode = "authorization.role.notFound"

  def apply(id: String) = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode, Map("id" -> id))
  )

}

object RoleAlreadyExist {
  val ErrorCode = TransportErrorCode.BadRequest
  val MessageCode = "authorization.role.alreadyExist"

  def apply(id: String) = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode, Map("id" -> id))
  )

}
