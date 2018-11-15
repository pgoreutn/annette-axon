package annette.security.user
import annette.shared.exceptions.AnnetteException

class UserServiceRequestException(status: Int, statusText: String)
  extends AnnetteException("core.userService.requestFailure",
    Map("status" -> status.toString, "statusText" -> statusText))


class UserServiceUauthorizedException(status: Int, statusText: String)
  extends AnnetteException("core.userService.unauthorized",
    Map("status" -> status.toString, "statusText" -> statusText))