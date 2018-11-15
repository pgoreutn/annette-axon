package annette.security.user
import annette.security.auth.UserId
import play.api.libs.json.{Format, Json}

case class User(
    id: UserId,
    firstName: String,
    lastName: String,
    username: String,
    email: String,
    emailVerified: Boolean,
    enabled: Boolean//,
    //attributes: Map[String, Seq[String]]
)

object User {
  implicit val format: Format[User] = Json.format
}
