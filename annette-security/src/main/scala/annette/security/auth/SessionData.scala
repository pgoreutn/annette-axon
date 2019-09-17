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

package annette.security.auth
import annette.authorization.api.model.Permission
import annette.security.auth.authorization._

case class SessionData(principal: UserPrincipal, authorizationResult: AuthorizationResult = AuthorizationResult())

case class UserPrincipal(
    userId: UserId,
    username: String,
    firstName: String,
    lastName: String,
    email: String,
    token: String,
    superUser: Boolean = false
)

case class AuthorizationResult(
    checkRule: CheckRule = DontCheck,
    checked: Boolean = false,
    operator: Condition = OR,
    findRule: FindRule = DontFind,
    found: Set[Permission] = Set.empty
)
