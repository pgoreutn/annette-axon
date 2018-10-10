package annette.shared.security
import annette.shared.exceptions.AnnetteException

class AuthenticationFailedException() extends AnnetteException("core.authentication.failed") {}
