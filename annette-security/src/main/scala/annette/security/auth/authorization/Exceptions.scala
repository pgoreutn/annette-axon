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

import annette.authorization.api.model.Permission
import annette.shared.exceptions.AnnetteException

class AuthorizationFailedException() extends AnnetteException("core.authorization.failed")

class RequiredAllPermissionException(permissions: Set[Permission])
    extends AnnetteException(
      "core.authorization.requiredAllPermissions",
      Map("permissions" -> permissions.map(_.toString).mkString(", "))
    )

class RequiredAnyPermissionException(permissions: Set[Permission])
    extends AnnetteException(
      "core.authorization.requiredAnyPermissions",
      Map("permissions" -> permissions.map(_.toString).mkString(", "))
    )
