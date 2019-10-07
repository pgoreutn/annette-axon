package annette.authorization.api.model

import play.api.libs.json.{Format, Json}

case class PrincipalAssignment (principal: AuthorizationPrincipal, roleId: RoleId) {
  def id = s"type-${principal.principalType}-id-${principal.principalId}-roleId-$roleId"
}

object PrincipalAssignment {
  implicit val format: Format[PrincipalAssignment] = Json.format
}
