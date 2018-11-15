package annette.security
import annette.authorization.api.Permission
import annette.security.authorization._

case class SessionData(principal: UserPrincipal, authorizationResult: AuthorizationResult = AuthorizationResult())

case class UserPrincipal(
                          userId: UserId,
                          username: String,
                          firstName: String,
                          lastName: String,
                          email: String,
                          superUser: Boolean = false
                        )

case class AuthorizationResult(
    checkRule: CheckRule = DontCheck,
    checked: Boolean = false,
    operator: Condition = OR,
    findRule: FindRule = DontFind,
    found: Set[Permission] = Set.empty
)
