package axon.knowledge.repository.api

import akka.{Done, NotUsed}
import annette.shared.exceptions.AnnetteExceptionSerializer
import axon.knowledge.repository.api.model.{DataSchema, DataSchemaKey, DataSchemaSummary}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.Environment

import scala.collection._

trait KnowledgeRepositoryService extends Service {

  def createDataSchema: ServiceCall[DataSchema, DataSchema]
  def updateDataSchema: ServiceCall[DataSchema, DataSchema]
  def deleteDataSchema(key: DataSchemaKey): ServiceCall[NotUsed, Done]
  def findDataSchemaByKey(key: String): ServiceCall[NotUsed, DataSchema]
  def findDataSchema: ServiceCall[String, immutable.Seq[DataSchemaSummary]]
  def findDataSchemaByKeys: ServiceCall[immutable.Seq[DataSchemaKey], immutable.Seq[DataSchemaSummary]]

  final override def descriptor = {
    import Service._
    // @formatter:off
    named("knowledge-repository")
      .withCalls(
        restCall(Method.POST, "/api/knowledge/repository/dataSchema", createDataSchema),
        restCall(Method.PUT, "/api/knowledge/repository/dataSchema", updateDataSchema),
        restCall(Method.DELETE, "/api/knowledge/repository/dataSchema/:key", deleteDataSchema _),
        restCall(Method.GET, "/api/knowledge/repository/dataSchema/:key", findDataSchemaByKey _),
        restCall(Method.POST, "/api/knowledge/repository/findDataSchema", findDataSchema _),
        restCall(Method.POST, "/api/knowledge/repository/findDataSchemaByKeys", findDataSchemaByKeys _),
      )
      .withExceptionSerializer(new AnnetteExceptionSerializer())
      .withAutoAcl(true)
    // @formatter:on
  }
}
