package test.axon.bpm.repository.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import annette.shared.exceptions.AnnetteTransportException
import axon.bpm.repository.api.model.BpmDiagram
import axon.bpm.repository.impl.BpmRepositorySerializerRegistry
import axon.bpm.repository.impl.diagram._
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class BpmDiagramEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val system = ActorSystem("BpmEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(BpmRepositorySerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def withTestDriver(block: PersistentEntityTestDriver[BpmDiagramCommand, BpmDiagramEvent, Option[BpmDiagram]] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, "bpm-1")
    block(driver)
    // driver.getAllIssues should have size 0
  }

  "bpmDiagram entity" should {

    "create bpmDiagram" in withTestDriver { driver =>
      val cmd = BpmDiagram("id", "name", Some("description"), "BPMN", "bpmDiagram")
      val outcome = driver.run(CreateBpmDiagram(cmd))
      outcome.replies should contain only BpmDiagram("id", "name", Some("description"), "BPMN", "bpmDiagram")
      outcome.events should contain only BpmDiagramCreated(cmd)
    }

    "create bpmDiagram with existing id" in withTestDriver { driver =>
      val diagram = BpmDiagram("id", "name", Some("description"), "BPMN", "bpmDiagram")
      val cmd = CreateBpmDiagram(diagram)
      val outcome = driver.run(cmd)
      outcome.replies should contain only BpmDiagram("id", "name", Some("description"), "BPMN", "bpmDiagram")
      outcome.events should contain only BpmDiagramCreated(diagram)
      val outcome2 = driver.run(cmd)
      outcome2.events shouldBe empty
      outcome2.replies should have size 1
      outcome2.replies.head shouldBe a[AnnetteTransportException]
    }

    "update bpmDiagram" in withTestDriver { driver =>
      val diagram = BpmDiagram("id", "name", Some("description"), "BPMN", "bpmDiagram")
      val cmd = CreateBpmDiagram(diagram)
      val diagram2 = BpmDiagram("id", "name1", Some("description1"), "BPMN", "bpmDiagram")
      val cmd2 = UpdateBpmDiagram(diagram2)
      val outcome = driver.run(cmd)
      outcome.replies should contain only diagram
      outcome.events should contain only BpmDiagramCreated(diagram)
      val outcome2 = driver.run(cmd2)
      outcome2.replies should contain only diagram2
      outcome2.events should contain only BpmDiagramUpdated(diagram2)
    }

    "update bpmDiagram with non-existing id" in withTestDriver { driver =>
      val diagram = BpmDiagram("id", "name1", Some("description1"), "BPMN", "bpmDiagram")
      val cmd2 = UpdateBpmDiagram(diagram)
      val outcome2 = driver.run(cmd2)
      outcome2.events shouldBe empty
      outcome2.replies should have size 1
      outcome2.replies.head shouldBe a[AnnetteTransportException]
    }

    "delete bpmDiagram" in withTestDriver { driver =>
      val diagram = BpmDiagram("id", "name", Some("description"), "BPMN", "bpmDiagram")
      val cmd = CreateBpmDiagram(diagram)
      val outcome = driver.run(cmd)
      outcome.replies should contain only diagram
      outcome.events should contain only BpmDiagramCreated(diagram)
      val outcome2 = driver.run(DeleteBpmDiagram(diagram.id))
      outcome2.replies should contain only Done
      outcome2.events should contain only BpmDiagramDeleted(diagram.id)
      val outcome3 = driver.run(FindBpmDiagramById(diagram.id))
      outcome3.replies should contain only None
    }

    "find bpmDiagram by id" in withTestDriver { driver =>
      val diagram = BpmDiagram("id", "name", Some("description"), "BPMN", "bpmDiagram")
      val cmd = CreateBpmDiagram(diagram)
      val cmd2 = FindBpmDiagramById("id")
      val outcome = driver.run(cmd)
      outcome.replies should contain only diagram
      outcome.events should contain only BpmDiagramCreated(diagram)
      val outcome2 = driver.run(cmd2)
      outcome2.replies should contain only Some(diagram)
    }

    "find bpmDiagram by non-existing id" in withTestDriver { driver =>
      val cmd2 = FindBpmDiagramById("id")
      val outcome2 = driver.run(cmd2)
      outcome2.replies should contain only None
    }

  }
}
