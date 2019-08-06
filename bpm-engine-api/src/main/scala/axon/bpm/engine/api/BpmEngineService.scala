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

package axon.bpm.engine.api

import akka.NotUsed
import annette.shared.exceptions.AnnetteExceptionSerializer
import axon.bpm.repository.api._
import axon.bpm.repository.api.model.BpmDiagram
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.JsObject

import scala.collection._

trait BpmEngineService extends Service {

  def findProcessDef: ServiceCall[FindProcessDefOptions, immutable.Seq[ProcessDef]]
  def findProcessDefByIds: ServiceCall[immutable.Seq[String], immutable.Seq[ProcessDef]]
  def findProcessDefByKeys: ServiceCall[immutable.Seq[String], immutable.Seq[ProcessDef]]
  def findCaseDef: ServiceCall[FindCaseDefOptions, immutable.Seq[CaseDef]]
  def findDecisionDef: ServiceCall[FindDecisionDefOptions, immutable.Seq[DecisionDef]]
  def deploy: ServiceCall[BpmDiagram, DeploymentWithDefs]

  final override def descriptor = {
    import Service._
    // @formatter:off
    named("bpm-engine")
      .withCalls(
        restCall(Method.POST, "/api/bpm/engine/repository/findProcessDef", findProcessDef),
        restCall(Method.POST, "/api/bpm/engine/repository/findProcessDefByIds", findProcessDefByIds),
        restCall(Method.POST, "/api/bpm/engine/repository/findProcessDefByKeys", findProcessDefByKeys),
        restCall(Method.POST, "/api/bpm/engine/repository/findCaseDef", findCaseDef),
        restCall(Method.POST, "/api/bpm/engine/repository/findDecisionDef", findDecisionDef),
        restCall(Method.POST, "/api/bpm/engine/repository/deploy", deploy),
//        restCall(Method.PUT, "/api/bpm/repository/schema", updateSchema),
//        restCall(Method.DELETE, "/api/bpm/repository/schema/:id", deleteSchema _),
//        restCall(Method.GET, "/api/bpm/repository/schema/:id", findSchemaById _),
//        restCall(Method.POST, "/api/bpm/repository/findSchema", findSchemas _)
      )
      .withExceptionSerializer(new AnnetteExceptionSerializer())
      .withAutoAcl(true)
    // @formatter:on
  }
}
