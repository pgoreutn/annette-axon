package axon.bpm.engine.api

import annette.shared.exceptions.AnnetteExceptionSerializer
import axon.bpm.repository.api._
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

import scala.collection._

trait BpmEngineService extends Service {

  def findProcessDef: ServiceCall[FindProcessDefOptions, immutable.Seq[ProcessDef]]
  def deploy: ServiceCall[Schema, DeploymentWithDefs]
//  def updateSchema: ServiceCall[Schema, Schema]
//  def deleteSchema(id: SchemaId): ServiceCall[NotUsed, Done]
//  def findSchemaById(id: String): ServiceCall[NotUsed, Schema]
//  def findSchemas: ServiceCall[String, immutable.Seq[SchemaSummary]]

  final override def descriptor = {
    import Service._
    // @formatter:off
    named("bpm-engine")
      .withCalls(
        restCall(Method.POST, "/api/bpm/engine/repository/findProcessDef", findProcessDef),
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
