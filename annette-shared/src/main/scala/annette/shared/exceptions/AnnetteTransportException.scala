package annette.shared.exceptions
import com.lightbend.lagom.scaladsl.api.transport.{ExceptionMessage, TransportErrorCode, TransportException}
import play.api.libs.json.{Format, Json}

final class AnnetteTransportException(errorCode: TransportErrorCode, annetteException: AnnetteException)
    extends TransportException(errorCode, new ExceptionMessage(annetteException.code, annetteException.toDetails), annetteException)

/*object AnnetteTransportException {
  implicit val format: Format[AnnetteTransportException] = Json.format
}*/
