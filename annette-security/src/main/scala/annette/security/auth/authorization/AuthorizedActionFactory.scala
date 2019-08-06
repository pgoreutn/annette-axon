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
