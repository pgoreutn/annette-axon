package annette.shared.security.authentication

import annette.shared.exceptions.AnnetteException

class AuthenticationFailedException() extends AnnetteException("core.authentication.failed") {}
