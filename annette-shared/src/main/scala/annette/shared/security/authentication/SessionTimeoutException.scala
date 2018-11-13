package annette.shared.security.authentication

import annette.shared.exceptions.AnnetteException

class SessionTimeoutException() extends AnnetteException("core.authentication.timeout") {}
