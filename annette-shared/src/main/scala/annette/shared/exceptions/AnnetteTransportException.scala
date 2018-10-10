package annette.shared.exceptions
import com.lightbend.lagom.scaladsl.api.transport.{ExceptionMessage, TransportErrorCode, TransportException}

final class AnnetteTransportException(errorCode: TransportErrorCode, annetteException: AnnetteException)
    extends TransportException(errorCode, new ExceptionMessage(annetteException.code, annetteException.toDetails), annetteException)
