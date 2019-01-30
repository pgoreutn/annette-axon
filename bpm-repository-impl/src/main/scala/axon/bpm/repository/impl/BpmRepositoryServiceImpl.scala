package axon.bpm.repository.impl

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.{Done, NotUsed}
import axon.bpm.repository.impl.bpmDiagram._
import axon.bpm.repository.api._
import axon.bpm.repository.impl.bpmDiagram._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.{BadRequest, NotFound}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.xml.XML

/**
  * Implementation of the BpmService.
  */
class BpmRepositoryServiceImpl(registry: PersistentEntityRegistry, system: ActorSystem, bpmDiagramRepository: BpmDiagramRepository)(
    implicit ec: ExecutionContext,
    mat: Materializer)
    extends BpmRepositoryService {

  override def createBpmDiagram: ServiceCall[BpmDiagram, BpmDiagram] = ServiceCall { bpmDiagram =>
    refFor(bpmDiagram.id)
      .ask(CreateBpmDiagram(bpmDiagram))
  }

  override def updateBpmDiagram: ServiceCall[BpmDiagram, BpmDiagram] = ServiceCall { bpmDiagram =>
    refFor(bpmDiagram.id)
      .ask(UpdateBpmDiagram(bpmDiagram))
  }

  override def deleteBpmDiagram(id: BpmDiagramId): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    refFor(id).ask(DeleteBpmDiagram(id))
  }
  override def findBpmDiagramById(id: String): ServiceCall[NotUsed, BpmDiagram] = ServiceCall { _ =>
    refFor(id).ask(FindBpmDiagramById(id)).map {
      case Some(bpmDiagram) => bpmDiagram
      case None             => throw BpmDiagramNotFound(id)
    }
  }
  override def findBpmDiagrams: ServiceCall[String, immutable.Seq[BpmDiagramSummary]] = ServiceCall { filter =>
    // TODO: temporary solution, should be implemented using ElasticSearch
    bpmDiagramRepository.findBpmDiagrams(filter.trim)
  }

  private def refFor(id: BpmDiagramId) = {
    if (id.trim.length != 0) {
      registry.refFor[BpmDiagramEntity](id)
    } else {
      throw IdRequired()
    }
  }

//  private def parseXml(bpmDiagram: String): BpmDiagram = {
//    val xml = Try(XML.loadString(bpmDiagram)).getOrElse(throw XmlParseError())
//    println(xml)
//    val r = Try {
//      val id = (xml \\ "process" \ "@id").text
//      if (id.trim.isEmpty) throw XmlParseError()
//      val name = (xml \\ "process" \ "@name").text
//      val description = (xml \\ "process" \ "documentation").text
//      val descriptionOpt = if (description.isEmpty) None else Some(description)
//      val notation = "BPMN"
//      BpmDiagram(id, name, descriptionOpt, notation, bpmDiagram)
//    }.getOrElse(throw XmlParseError())
//    println(r)
//    r
//  }

}
