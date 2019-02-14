package axon.bpm.repository.api

import akka.{Done, NotUsed}
import annette.shared.exceptions.AnnetteExceptionSerializer
import axon.bpm.repository.api.model._
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

import scala.collection._

trait BpmRepositoryService extends Service {

  def createBpmDiagram: ServiceCall[BpmDiagram, BpmDiagram]
  def updateBpmDiagram: ServiceCall[BpmDiagram, BpmDiagram]
  def deleteBpmDiagram(id: BpmDiagramId): ServiceCall[NotUsed, Done]
  def findBpmDiagramById(id: String): ServiceCall[NotUsed, BpmDiagram]
  def findBpmDiagrams: ServiceCall[String, immutable.Seq[BpmDiagramSummary]]

  def createBusinessProcess: ServiceCall[BusinessProcess, BusinessProcess]
  def updateBusinessProcess: ServiceCall[BusinessProcess, BusinessProcess]
  def deleteBusinessProcess(key: BusinessProcessKey): ServiceCall[NotUsed, Done]
  def findBusinessProcessByKey(key: String): ServiceCall[NotUsed, BusinessProcess]
  def findBusinessProcess: ServiceCall[String, immutable.Seq[BusinessProcessSummary]]

  final override def descriptor = {
    import Service._
    // @formatter:off
    named("bpm-repository")
      .withCalls(
        restCall(Method.POST, "/api/bpm/repository/bpmDiagram", createBpmDiagram),
        restCall(Method.PUT, "/api/bpm/repository/bpmDiagram", updateBpmDiagram),
        restCall(Method.DELETE, "/api/bpm/repository/bpmDiagram/:id", deleteBpmDiagram _),
        restCall(Method.GET, "/api/bpm/repository/bpmDiagram/:id", findBpmDiagramById _),
        restCall(Method.POST, "/api/bpm/repository/findBpmDiagram", findBpmDiagrams _),

        restCall(Method.POST, "/api/bpm/repository/businessProcess", createBusinessProcess),
        restCall(Method.PUT, "/api/bpm/repository/businessProcess", updateBusinessProcess),
        restCall(Method.DELETE, "/api/bpm/repository/businessProcess/:key", deleteBusinessProcess _),
        restCall(Method.GET, "/api/bpm/repository/businessProcess/:key", findBusinessProcessByKey _),
        restCall(Method.POST, "/api/bpm/repository/findBusinessProcess", findBusinessProcess _)
      )
      .withExceptionSerializer(new AnnetteExceptionSerializer())
      .withAutoAcl(true)
    // @formatter:on
  }
}
