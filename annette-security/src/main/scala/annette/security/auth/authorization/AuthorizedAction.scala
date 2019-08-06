/*
 * Copyright 2018 Valery Lobachev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
