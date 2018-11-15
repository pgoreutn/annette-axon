package annette.security.auth.authorization

import annette.authorization.api._
import annette.security.auth.SessionData
import javax.inject._
import play.api.mvc.Request

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DefaultAuthorizer @Inject()(authorizationService: AuthorizationService) extends Authorizer {

  override def authorize[A](request: Request[A], sessionData: SessionData, roles: Set[RoleId], authorizationQuery: AuthorizationQuery)(
      implicit ec: ExecutionContext): Future[SessionData] = {
    println(s"DefaultAuthorizer.authorize: $authorizationQuery")
    authorizationQuery match {
      case AuthorizationQuery(DontCheck, _, DontFind) =>
        println("throw AuthorizationFailedException")
        throw new AuthorizationFailedException()
      case AuthorizationQuery(_, OR, _) =>
        processOr(sessionData, roles, authorizationQuery)
      case AuthorizationQuery(_, AND, _) =>
        processAnd(sessionData, roles, authorizationQuery)
    }
  }

  def processOr(sessionData: SessionData, roles: Set[RoleId], authorizationQuery: AuthorizationQuery)(
      implicit ec: ExecutionContext): Future[SessionData] = {
    for {
      sessionDataAfterCheck <- checkPermissions(sessionData, roles, authorizationQuery)
      _ = if (!sessionDataAfterCheck.authorizationResult.checked &&
              authorizationQuery.findRule == DontFind &&
              !sessionDataAfterCheck.principal.superUser) {
        authorizationFailed(authorizationQuery)
      }
      resultSessionData <- findPermissions(sessionDataAfterCheck, roles, authorizationQuery)
    } yield resultSessionData
  }

  def processAnd(sessionData: SessionData, roles: Set[RoleId], authorizationQuery: AuthorizationQuery)(
      implicit ec: ExecutionContext): Future[SessionData] = {
    for {
      sessionDataAfterCheck <- checkPermissions(sessionData, roles, authorizationQuery)
      _ = if (!sessionDataAfterCheck.authorizationResult.checked) {
        authorizationFailed(authorizationQuery)
      }
      resultSessionData <- findPermissions(sessionDataAfterCheck, roles, authorizationQuery)
    } yield resultSessionData
  }

  def checkPermissions(sessionData: SessionData, roles: Set[RoleId], authorizationQuery: AuthorizationQuery)(
      implicit ec: ExecutionContext): Future[SessionData] = {
    for {
      checked <- if (sessionData.principal.superUser) {
        Future.successful(true)
      } else {
        authorizationQuery.checkRule match {
          case DontCheck =>
            Future.successful(false)
          case CheckAllRule(permissions) =>
            authorizationService.checkAllPermissions.invoke(CheckPermissions(roles, permissions))
          case CheckAnyRule(permissions) =>
            authorizationService.checkAnyPermissions.invoke(CheckPermissions(roles, permissions))
        }
      }
    } yield {
      val authResult = sessionData.authorizationResult.copy(
        checkRule = authorizationQuery.checkRule,
        checked = checked
      )
      sessionData.copy(authorizationResult = authResult)
    }
  }

  def findPermissions(sessionData: SessionData, roles: Set[RoleId], authorizationQuery: AuthorizationQuery)(
      implicit ec: ExecutionContext): Future[SessionData] = {
    for {
      found <- authorizationQuery.findRule match {
        case DontFind =>
          Future.successful(Set.empty[Permission])
        case FindPermissionsRule(permissionIds) =>
          authorizationService.findPermissions.invoke(FindPermissions(roles, permissionIds))
      }
    } yield {
      val authResult = sessionData.authorizationResult.copy(
        findRule = authorizationQuery.findRule,
        found = found
      )
      sessionData.copy(authorizationResult = authResult)
    }

  }

  private def authorizationFailed(authorizationQuery: AuthorizationQuery) = {
    authorizationQuery.checkRule match {
      case DontCheck =>
        throw new AuthorizationFailedException()
      case CheckAllRule(permissions) =>
        throw new RequiredAllPermissionException(permissions)
      case CheckAnyRule(permissions) =>
        throw new RequiredAnyPermissionException(permissions)
    }
  }

}
