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

import java.io.{CharArrayWriter, PrintWriter}

import akka.util.ByteString
import com.lightbend.lagom.scaladsl.api.deser.{ExceptionSerializer, RawExceptionMessage}
import com.lightbend.lagom.scaladsl.api.transport._
import play.api.libs.json._

import scala.collection.immutable.Seq
import scala.util.Try
import scala.util.control.NonFatal

class AnnetteExceptionSerializer extends ExceptionSerializer {

  override def serialize(exception: Throwable, accept: Seq[MessageProtocol]): RawExceptionMessage = {

    //println("AnnetteExceptionSerializer: serialize")

    val (errorCode, message) = exception match {

      case te: TransportException =>
        (te.errorCode, te.exceptionMessage)
      /*case e if environment.mode == Mode.Prod =>
        // By default, don't give out information about generic exceptions.
        (TransportErrorCode.InternalServerError, new ExceptionMessage("Exception", ""))*/
      case e =>
        // Ok to give out exception information in dev and test
        val writer = new CharArrayWriter
        e.printStackTrace(new PrintWriter(writer))
        val detail = writer.toString
        (TransportErrorCode.InternalServerError, new ExceptionMessage(s"${exception.getClass.getName}: ${exception.getMessage}", detail))
    }

    val messageBytes = ByteString.fromString(
      Json.stringify(
        Json.obj(
          "name" -> message.name,
          "detail" -> message.detail
        )))

    RawExceptionMessage(errorCode, MessageProtocol(Some("application/json"), None, None), messageBytes)
  }

  override def deserialize(message: RawExceptionMessage): Throwable = {
    //println("AnnetteExceptionSerializer: deserialize")

    val messageJson = try {
      Json.parse(message.message.iterator.asInputStream)
    } catch {
      case NonFatal(e) =>
        Json.obj()
    }

    val jsonParseResult = for {
      name <- (messageJson \ "name").validate[String]
      detail <- (messageJson \ "detail").validate[String]
    } yield new ExceptionMessage(name, detail)

    val exceptionMessage = jsonParseResult match {
      case JsSuccess(m, _) => m
      case JsError(_)      => new ExceptionMessage("UndeserializableException", message.message.utf8String)
    }

    fromCodeAndMessage(message.errorCode, exceptionMessage)
  }

  /**
    * Override this if you wish to deserialize your own custom Exceptions.
    *
    * The default implementation delegates to [[TransportException.fromCodeAndMessage()]], which will return a best match
    * Lagom built-in exception.
    *
    * @param transportErrorCode The transport error code.
    * @param exceptionMessage The exception message.
    * @return The exception.
    */
  protected def fromCodeAndMessage(transportErrorCode: TransportErrorCode, exceptionMessage: ExceptionMessage): Throwable = {

    Try { new AnnetteException(exceptionMessage.name, exceptionMessage.detail) }
      .getOrElse(TransportException.fromCodeAndMessage(transportErrorCode, exceptionMessage))

  }
}
