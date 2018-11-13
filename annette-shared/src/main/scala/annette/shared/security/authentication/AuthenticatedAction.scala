/***************************************************************************************
  * Copyright (c) 2014-2017 by Valery Lobachev
  * Redistribution and use in source and binary forms, with or without
  * modification, are NOT permitted without written permission from Valery Lobachev.
  *
  * Copyright (c) 2014-2017 Валерий Лобачев
  * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
  * запрещено без письменного разрешения правообладателя.
  ****************************************************************************************/
package annette.shared.security.authentication

import annette.shared.exceptions.{AnnetteException, AnnetteThrowable}
import annette.shared.security.{AbstractAuthAction, AuthenticatedRequest, SessionData}
import javax.inject._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
@Singleton
class AuthenticatedAction @Inject()(
    authenticator: Authenticator,
    override val parser: BodyParsers.Default,
    implicit override val executionContext: ExecutionContext
) extends AbstractAuthAction(parser, executionContext) {
  override def validate[A](request: Request[A]): Future[SessionData] = authenticator.authenticate(request)
}
