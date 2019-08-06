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

package axon.rest.bpm.repository

import annette.security.auth.authentication.AuthenticatedAction
import annette.security.auth.authorization.{AuthorizedActionFactory, CheckAny}
import annette.shared.exceptions.AnnetteException
import axon.bpm.engine.api.{BpmEngineService, FindDecisionDefOptions, FindProcessDefOptions}
import axon.bpm.repository.api.BpmRepositoryService
import axon.rest.bpm.permission.BpmPermissions._
import javax.inject.Inject
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

class BpmDeploymentController @Inject()(
    authenticated: AuthenticatedAction,
    authorized: AuthorizedActionFactory,
    bpmRepositoryService: BpmRepositoryService,
    bpmEngineService: BpmEngineService,
    cc: ControllerComponents,
    implicit val ec: ExecutionContext)
    extends AbstractController(cc) {

  def find() = authorized(CheckAny(PROCESS_DEF_VIEW)).async(parse.json[FindProcessDefOptions]) { implicit request =>
    val filter = request.body
    bpmEngineService.findProcessDef
      .invoke(filter)
      .map(r => Ok(Json.toJson(r)))
      .recover {
        case ex: AnnetteException =>
          BadRequest(ex.toMessage)
      }
  }

  def deploy(id: String) = authorized(CheckAny(BPM_REPOSITORY_CONTROL)).async { implicit request =>
    (for {
      bpmDiagram <- bpmRepositoryService
        .findBpmDiagramById(id)
        .invoke()
      deployment <- bpmEngineService.deploy.invoke(bpmDiagram)
    } yield {
      Ok(Json.toJson(deployment))
    }).recover {
      case ex: AnnetteException =>
        BadRequest(ex.toMessage)
    }
  }
}
