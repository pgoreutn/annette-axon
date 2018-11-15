package annette.security.auth.authorization

import annette.authorization.api.{Permission, PermissionId}

case class AuthorizationQuery(
    checkRule: CheckRule = DontCheck,
    condition: Condition = OR,
    findRule: FindRule = DontFind
) {

  def or(authorizationQuery: AuthorizationQuery): AuthorizationQuery = {
    copyQuery(OR, authorizationQuery)
  }

  def and(authorizationQuery: AuthorizationQuery): AuthorizationQuery = {
    copyQuery(AND, authorizationQuery)
  }

  private def copyQuery(op: Condition, authorizationQuery: AuthorizationQuery): AuthorizationQuery = {

    if (checkRule.isInstanceOf[DontCheck.type]) {
      throw new Exception("Check rule in first operand should be defined")
    }
    if (!authorizationQuery.checkRule.isInstanceOf[DontCheck.type]) {
      throw new Exception("Check rule in second operand should NOT be defined")
    }
    if (!findRule.isInstanceOf[DontFind.type]) {
      throw new Exception("Find rule in first operand should NOT be defined")
    }
    if (authorizationQuery.findRule.isInstanceOf[DontFind.type]) {
      throw new Exception("Find rule in second operand should be defined")
    }

    copy(condition = op, findRule = authorizationQuery.findRule)
  }

}

sealed trait CheckRule
case object DontCheck extends CheckRule
case class CheckAllRule(permissions: Set[Permission]) extends CheckRule
case class CheckAnyRule(permissions: Set[Permission]) extends CheckRule

sealed trait FindRule
case object DontFind extends FindRule
case class FindPermissionsRule(permissionIds: Set[PermissionId]) extends FindRule

sealed trait Condition
case object OR extends Condition
case object AND extends Condition

object CheckAll {
  def apply(permissions: Permission*): AuthorizationQuery = AuthorizationQuery(CheckAllRule(Set(permissions: _*)))
}

object CheckAny {
  def apply(permissions: Permission*): AuthorizationQuery = AuthorizationQuery(CheckAnyRule(Set(permissions: _*)))
}

object Find {
  def apply(permissionIds: PermissionId*): AuthorizationQuery =
    AuthorizationQuery(findRule = FindPermissionsRule(Set(permissionIds: _*)))
}

object test {

  val q1 = CheckAll(Permission("a"), Permission("b"))
  val q2 = Find("p1", "p2")
  val q3 = CheckAll(Permission("a"), Permission("b")) or Find("p1", "p2")
  val q4 = CheckAll(Permission("a"), Permission("b")) and Find("p1", "p2")

}
