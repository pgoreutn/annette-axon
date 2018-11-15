package annette.security.user
import play.api.libs.json.{Format, Json}

case class UserQuery(
    search: Option[String] = None, // A String contained in username, first or last name, or email
    firstName: Option[String] = None,
    lastName: Option[String] = None,
    email: Option[String] = None,
    username: Option[String] = None,
    briefRepresentation: Option[Boolean] = Some(false),
    max: Option[Int] = Some(100),
    first: Option[Int] = None
)

object UserQuery {
  implicit val format: Format[UserQuery] = Json.format
}
