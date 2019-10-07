package annette.authorization.api.model

import play.api.libs.json.{Format, Json}

case class AuthorizationPrincipal (principalType: PrincipalType, principalId: PrincipalId)

object AuthorizationPrincipal {
  implicit val format: Format[AuthorizationPrincipal] = Json.format
}
