/***************************************************************************************
  * Copyright (c) 2014-2017 by Valery Lobachev
  * Redistribution and use in source and binary forms, with or without
  * modification, are NOT permitted without written permission from Valery Lobachev.
  *
  * Copyright (c) 2014-2017 Валерий Лобачев
  * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
  * запрещено без письменного разрешения правообладателя.
  ****************************************************************************************/
package annette.security.auth

import annette.shared.exceptions.{AnnetteException, AnnetteThrowable}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

abstract class AbstractAuthAction(val parser: BodyParsers.Default, implicit val executionContext: ExecutionContext)
    extends ActionBuilder[AuthenticatedRequest, AnyContent] {

  def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
    validate(request)
      .map(sessionData => block(AuthenticatedRequest[A](sessionData, request)))
      .recover {
        case throwable: Throwable =>
          notAuthenticated(request, throwable)
      }
      .flatMap(result => result)
  }

  def validate[A](request: Request[A]): Future[SessionData]

  protected def notAuthenticated[A](request: Request[A], throwable: Throwable): Future[Result] = Future.successful {
    throwable match {
      case exception: AnnetteException =>
        Results.Unauthorized(exception.toMessage)
      case _ =>
        Results.Unauthorized(new AnnetteThrowable(throwable).toMessage)
    }
  }

}
