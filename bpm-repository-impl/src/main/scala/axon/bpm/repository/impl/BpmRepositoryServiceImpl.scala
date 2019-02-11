package axon.bpm.repository.impl

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.{Done, NotUsed}
import axon.bpm.repository.api._
import axon.bpm.repository.api.model._
import axon.bpm.repository.impl.diagram._
import axon.bpm.repository.impl.process._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.collection.immutable
import scala.concurrent.ExecutionContext

/**
  * Implementation of the BpmService.
  */
class BpmRepositoryServiceImpl(
    registry: PersistentEntityRegistry,
    system: ActorSystem,
    bpmDiagramRepository: BpmDiagramRepository,
    businessProcessRepository: BusinessProcessRepository,
)(implicit ec: ExecutionContext, mat: Materializer)
    extends BpmRepositoryService {

  override def createBpmDiagram: ServiceCall[BpmDiagram, BpmDiagram] = ServiceCall { bpmDiagram =>
    bpmDiagramRefFor(bpmDiagram.id)
      .ask(CreateBpmDiagram(bpmDiagram))
  }

  override def updateBpmDiagram: ServiceCall[BpmDiagram, BpmDiagram] = ServiceCall { bpmDiagram =>
    bpmDiagramRefFor(bpmDiagram.id)
      .ask(UpdateBpmDiagram(bpmDiagram))
  }

  override def deleteBpmDiagram(id: BpmDiagramId): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    bpmDiagramRefFor(id).ask(DeleteBpmDiagram(id))
  }
  override def findBpmDiagramById(id: String): ServiceCall[NotUsed, BpmDiagram] = ServiceCall { _ =>
    bpmDiagramRefFor(id).ask(FindBpmDiagramById(id)).map {
      case Some(bpmDiagram) => bpmDiagram
      case None             => throw BpmDiagramNotFound(id)
    }
  }
  override def findBpmDiagrams: ServiceCall[String, immutable.Seq[BpmDiagramSummary]] = ServiceCall { filter =>
    // TODO: temporary solution, should be implemented using ElasticSearch
    bpmDiagramRepository.findBpmDiagrams(filter.trim)
  }

  private def bpmDiagramRefFor(id: BpmDiagramId) = {
    if (id.trim.length != 0) {
      registry.refFor[BpmDiagramEntity](id)
    } else {
      throw BpmDiagramIdRequired()
    }
  }

  override def createBusinessProcess: ServiceCall[BusinessProcess, BusinessProcess] = ServiceCall { businessProcess =>
    businessProcessRefFor(businessProcess.id)
      .ask(CreateBusinessProcess(businessProcess))
  }

  override def updateBusinessProcess: ServiceCall[BusinessProcess, BusinessProcess] = ServiceCall { businessProcess =>
    businessProcessRefFor(businessProcess.id)
      .ask(UpdateBusinessProcess(businessProcess))
  }

  override def deleteBusinessProcess(id: BusinessProcessId): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    businessProcessRefFor(id).ask(DeleteBusinessProcess(id))
  }
  override def findBusinessProcessById(id: String): ServiceCall[NotUsed, BusinessProcess] = ServiceCall { _ =>
    businessProcessRefFor(id).ask(FindBusinessProcessById(id)).map {
      case Some(businessProcess) => businessProcess
      case None                  => throw BusinessProcessNotFound(id)
    }
  }
  override def findBusinessProcess: ServiceCall[String, immutable.Seq[BusinessProcessSummary]] = ServiceCall { filter =>
    // TODO: temporary solution, should be implemented using ElasticSearch
    businessProcessRepository.findBusinessProcess(filter.trim)
  }

  private def businessProcessRefFor(id: BusinessProcessId) = {
    if (id.trim.length != 0) {
      registry.refFor[BusinessProcessEntity](id)
    } else {
      throw BusinessProcessIdRequired()
    }
  }

}
