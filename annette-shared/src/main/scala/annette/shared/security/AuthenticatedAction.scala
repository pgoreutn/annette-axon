/***************************************************************************************
  * Copyright (c) 2014-2017 by Valery Lobachev
  * Redistribution and use in source and binary forms, with or without
  * modification, are NOT permitted without written permission from Valery Lobachev.
  *
  * Copyright (c) 2014-2017 Валерий Лобачев
  * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
  * запрещено без письменного разрешения правообладателя.
  ****************************************************************************************/
package annette.shared.security

import annette.shared.exceptions.{AnnetteException, AnnetteThrowable}
import javax.inject._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}


case class AuthenticatedRequest[A](sessionData: SessionData, request: Request[A]) extends WrappedRequest[A](request)

@Singleton
class AuthenticatedAction @Inject()(requestValidator: RequestValidator, val parser: BodyParsers.Default, implicit val executionContext: ExecutionContext)
    extends ActionBuilder[AuthenticatedRequest, AnyContent] {

  def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {

    requestValidator
      .validate(request)
      .map(sessionData => block(AuthenticatedRequest[A](sessionData, request)))
      .recover {
        case throwable: Throwable =>
          notAuthenticated(request, throwable)
      }
      .flatMap(result => result)

  }

  protected def notAuthenticated[A](request: Request[A], throwable: Throwable): Future[Result] = Future.successful {
    throwable match {
      case exception: AnnetteException =>
        Results.Unauthorized(exception.toMessage)
      case _ =>
        Results.Unauthorized(new AnnetteThrowable(throwable).toMessage)
    }
  }

}
