package axon.bpm.repository.api

import akka.{Done, NotUsed}
import annette.shared.exceptions.AnnetteExceptionSerializer
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.Environment

import scala.collection._

trait BpmRepositoryService extends Service {

  def createBpmDiagram: ServiceCall[BpmDiagram, BpmDiagram]
  def updateBpmDiagram: ServiceCall[BpmDiagram, BpmDiagram]
  def deleteBpmDiagram(id: BpmDiagramId): ServiceCall[NotUsed, Done]
  def findBpmDiagramById(id: String): ServiceCall[NotUsed, BpmDiagram]
  def findBpmDiagrams: ServiceCall[String, immutable.Seq[BpmDiagramSummary]]

  final override def descriptor = {
    import Service._
    // @formatter:off
    named("bpm-repository")
      .withCalls(
        restCall(Method.POST, "/api/bpm/repository/bpmDiagram", createBpmDiagram),
        restCall(Method.PUT, "/api/bpm/repository/bpmDiagram", updateBpmDiagram),
        restCall(Method.DELETE, "/api/bpm/repository/bpmDiagram/:id", deleteBpmDiagram _),
        restCall(Method.GET, "/api/bpm/repository/bpmDiagram/:id", findBpmDiagramById _),
        restCall(Method.POST, "/api/bpm/repository/findBpmDiagram", findBpmDiagrams _)
      )
      .withExceptionSerializer(new AnnetteExceptionSerializer())
      .withAutoAcl(true)
    // @formatter:on
  }
}
