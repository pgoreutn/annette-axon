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

import annette.security.auth.authentication.Authenticator
import javax.inject._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
@Singleton
class AuthorizedActionFactory @Inject()(
    authenticator: Authenticator,
    roleProvider: RoleProvider,
    authorizer: Authorizer,
    parser: BodyParsers.Default,
    executionContext: ExecutionContext
) {

  def apply(newAuthorizationQuery: AuthorizationQuery) =
    new AuthorizedAction(authenticator, roleProvider, authorizer, newAuthorizationQuery, parser, executionContext)
}
