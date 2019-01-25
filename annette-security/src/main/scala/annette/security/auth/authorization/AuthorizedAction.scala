/***************************************************************************************
  * Copyright (c) 2014-2017 by Valery Lobachev
  * Redistribution and use in source and binary forms, with or without
  * modification, are NOT permitted without written permission from Valery Lobachev.
  *
  * Copyright (c) 2014-2017 Валерий Лобачев
  * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
  * запрещено без письменного разрешения правообладателя.
  ****************************************************************************************/
package annette.security.auth.authorization

import annette.security.auth.{AbstractAuthAction, SessionData}
import annette.security.auth.authentication.Authenticator
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class AuthorizedAction(
    authenticator: Authenticator,
    roleProvider: RoleProvider,
    authorizer: Authorizer,
    authorizationQuery: AuthorizationQuery,
    override val parser: BodyParsers.Default,
    implicit override val executionContext: ExecutionContext
) extends AbstractAuthAction(parser, executionContext) {
  override def validate[A](request: Request[A]): Future[SessionData] = {
//    println(s"AuthorizedAction.validate")
    for {
      authenticatedSessionData <- authenticator.authenticate(request)
//      _ = println(s"authenticatedSessionData: $authenticatedSessionData")
      roles <- roleProvider.get(authenticatedSessionData.principal.userId)
//      _ = println(s"roles: $roles")
      authorizedSessionData <- authorizer.authorize(request, authenticatedSessionData, roles, authorizationQuery)
//      _ = println(s"authorizedSessionData: $authorizedSessionData")
    } yield authorizedSessionData
  }

}
