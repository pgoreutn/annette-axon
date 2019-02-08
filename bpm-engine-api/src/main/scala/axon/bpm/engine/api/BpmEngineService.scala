package axon.bpm.engine.api

import annette.shared.exceptions.AnnetteExceptionSerializer
import axon.bpm.repository.api._
import axon.bpm.repository.api.model.BpmDiagram
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.JsObject

import scala.collection._

trait BpmEngineService extends Service {

  def findProcessDef: ServiceCall[FindProcessDefOptions, immutable.Seq[ProcessDef]]
  def findCaseDef: ServiceCall[FindCaseDefOptions, immutable.Seq[CaseDef]]
  def findDecisionDef: ServiceCall[FindDecisionDefOptions, immutable.Seq[DecisionDef]]
  def deploy: ServiceCall[BpmDiagram, DeploymentWithDefs]

  def test: ServiceCall[JsObject, String]

  final override def descriptor = {
    import Service._
    // @formatter:off
    named("bpm-engine")
      .withCalls(
        restCall(Method.POST, "/api/bpm/engine/repository/findProcessDef", findProcessDef),
        restCall(Method.POST, "/api/bpm/engine/repository/findCaseDef", findCaseDef),
        restCall(Method.POST, "/api/bpm/engine/repository/findDecisionDef", findDecisionDef),
        restCall(Method.POST, "/api/bpm/engine/repository/deploy", deploy),
        restCall(Method.POST, "/api/bpm/engine/repository/test", test),
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
