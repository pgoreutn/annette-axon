package annette.security.authentication

import annette.shared.exceptions.AnnetteException

class AuthenticationFailedException() extends AnnetteException("core.authentication.failed")

class SessionTimeoutException() extends AnnetteException("core.authentication.timeout")
