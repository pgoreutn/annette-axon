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
import annette.authorization.api.{AuthorizationService, RoleId}
import annette.security.auth.UserId
import javax.inject._

import scala.concurrent.Future

@Singleton
class DefaultRoleProvider @Inject()(authorizationService: AuthorizationService) extends RoleProvider {
  override def get(userId: UserId): Future[Set[RoleId]] = {
    authorizationService.findRolesAssignedToUser(userId).invoke()
  }
}
