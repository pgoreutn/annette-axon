/*
 * Copyright 2018 Valery Lobachev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package annette.security.user

import annette.security.auth.UserId
import annette.security.auth.authorization.AuthorizationFailedException
import annette.shared.exceptions.AnnetteException
import javax.inject._
import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserService @Inject()(ws: WSClient, configuration: Configuration) {
  val realm = configuration.get[String]("annette.security.client.realm")
  val url = configuration.get[String]("annette.security.client.keycloak-url")

  def findUsers(token: String, query: UserQuery)(implicit ec: ExecutionContext): Future[Seq[User]] = {

    val parameters: Seq[(String, String)] = Seq(
      query.search.map("search" -> _),
      query.firstName.map("firstName" -> _),
      query.lastName.map("lastName" -> _),
      query.email.map("email" -> _),
      query.username.map("username" -> _),
      query.briefRepresentation.map("briefRepresentation" -> _.toString),
      query.max.map("max" -> _.toString),
      query.first.map("first" -> _.toString)
    ).flatten
    val fullUrl = s"$url/admin/realms/$realm/users"

    ws.url(fullUrl)
      .addHttpHeaders(
        "Authorization" -> s"Bearer $token"
      )
      .addQueryStringParameters(parameters: _*)
      .get()
      .map { response =>
        response.status match {
          case 200 =>
            response.json.validate[Seq[User]].get
          case 401 =>
            throw new UserServiceUauthorizedException(response.status, response.statusText)
          case _ =>
            throw new UserServiceRequestException(response.status, response.statusText)
        }
      }
  }

  def findUserById(token: String, userId: UserId)(implicit ec: ExecutionContext): Future[Option[User]] = {
    ws.url(s"$url/admin/realms/$realm/users/$userId")
      .addHttpHeaders(
        "Authorization" -> s"Bearer $token"
      )
      .get()
      .map { response =>
        response.status match {
          case 200 =>
            val user = response.json.validate[User].get
            Some(user)
          case 404 =>
            None
          case 401 =>
            throw new UserServiceUauthorizedException(response.status, response.statusText)
          case _ =>
            throw new UserServiceRequestException(response.status, response.statusText)
        }
      }
  }

  def findUserByIds(token: String, userIds: Set[UserId])(implicit ec: ExecutionContext): Future[Set[User]] = {
    Future.traverse(userIds)(id => findUserById(token, id)).map(_.flatten)
  }

}
