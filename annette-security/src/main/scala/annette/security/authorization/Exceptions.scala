package annette.security.authorization

import annette.authorization.api.Permission
import annette.shared.exceptions.AnnetteException

class AuthorizationFailedException() extends AnnetteException("core.authorization.failed")

class RequiredAllPermissionException(permissions: Set[Permission])
  extends AnnetteException(
    "core.authorization.requiredAllPermissions",
    Map( "permissions" -> permissions.map(_.toString).mkString(", "))
  )

class RequiredAnyPermissionException(permissions: Set[Permission])
  extends AnnetteException(
    "core.authorization.requiredAnyPermissions",
    Map( "permissions" -> permissions.map(_.toString).mkString(", "))
  )
