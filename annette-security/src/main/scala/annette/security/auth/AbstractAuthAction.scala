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

package annette.security.auth

import annette.shared.exceptions.{AnnetteException, AnnetteThrowable}
import org.slf4j.{Logger, LoggerFactory}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

abstract class AbstractAuthAction(val parser: BodyParsers.Default, implicit val executionContext: ExecutionContext)
    extends ActionBuilder[AuthenticatedRequest, AnyContent] {
  private final val log: Logger = LoggerFactory.getLogger(classOf[AbstractAuthAction])

  def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    log.info("Request method={}, uri={}, ip={}", request.method, request.uri, request.connection.remoteAddressString )
    validate(request)
      .map(sessionData => block(AuthenticatedRequest[A](sessionData, request)))
      .recover {
        case throwable: Throwable =>
          notAuthenticated(request, throwable)
      }
      .flatMap(result => result)
  }

  def validate[A](request: Request[A]): Future[SessionData]

  protected def notAuthenticated[A](request: Request[A], throwable: Throwable): Future[Result] = Future.successful {
    throwable match {
      case exception: AnnetteException =>
        Results.Unauthorized(exception.toMessage)
      case _ =>
        Results.Unauthorized(new AnnetteThrowable(throwable).toMessage)
    }
  }

}
