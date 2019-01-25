package axon.bpm.repository.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import annette.shared.exceptions.AnnetteTransportException
import axon.bpm.repository.api.Schema
import axon.bpm.repository.impl.schema._
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class SchemaEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val system = ActorSystem("BpmEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(BpmRepositorySerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def withTestDriver(block: PersistentEntityTestDriver[SchemaCommand, SchemaEvent, Option[Schema]] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new SchemaEntity, "bpm-1")
    block(driver)
    // driver.getAllIssues should have size 0
  }

  "schema entity" should {

    "create schema" in withTestDriver { driver =>
      val cmd = CreateSchema("id", "name", Some("description"), "BPMN", "schema")
      val outcome = driver.run(cmd)
      outcome.replies should contain only Schema("id", "name", Some("description"), "BPMN", "schema")
      outcome.events should contain only SchemaCreated(cmd.id, cmd.name, cmd.description, cmd.notation, cmd.schema)
    }

    "create schema with existing id" in withTestDriver { driver =>
      val cmd = CreateSchema("id", "name", Some("description"), "BPMN", "schema")
      val outcome = driver.run(cmd)
      outcome.replies should contain only Schema("id", "name", Some("description"), "BPMN", "schema")
      outcome.events should contain only SchemaCreated(cmd.id, cmd.name, cmd.description, cmd.notation, cmd.schema)
      val outcome2 = driver.run(cmd)
      outcome2.events shouldBe empty
      outcome2.replies should have size 1
      outcome2.replies.head shouldBe a[AnnetteTransportException]
    }

    "update schema" in withTestDriver { driver =>
      val cmd = CreateSchema("id", "name", Some("description"), "BPMN", "schema")
      val cmd2 = UpdateSchema("id", "name1", Some("description1"), "BPMN", "schema")
      val outcome = driver.run(cmd)
      outcome.replies should contain only Schema("id", "name", Some("description"), "BPMN", "schema")
      outcome.events should contain only SchemaCreated(cmd.id, cmd.name, cmd.description, cmd.notation, cmd.schema)
      val outcome2 = driver.run(cmd2)
      outcome2.replies should contain only Schema("id", "name1", Some("description1"), "BPMN", "schema")
      outcome2.events should contain only SchemaUpdated(cmd2.id, cmd2.name, cmd2.description, cmd2.schema)
    }

    "update schema with non-existing id" in withTestDriver { driver =>
      val cmd2 = UpdateSchema("id", "name1", Some("description1"), "BPMN", "schema")
      val outcome2 = driver.run(cmd2)
      outcome2.events shouldBe empty
      outcome2.replies should have size 1
      outcome2.replies.head shouldBe a[AnnetteTransportException]
    }

    "delete schema" in withTestDriver { driver =>
      val cmd = CreateSchema("id", "name", Some("description"), "BPMN", "schema")
      val outcome = driver.run(cmd)
      outcome.replies should contain only Schema("id", "name", Some("description"), "BPMN", "schema")
      outcome.events should contain only SchemaCreated(cmd.id, cmd.name, cmd.description, cmd.notation, cmd.schema)
      val outcome2 = driver.run(DeleteSchema(cmd.id))
      outcome2.replies should contain only Done
      outcome2.events should contain only SchemaDeleted(cmd.id)
      val outcome3 = driver.run(FindSchemaById(cmd.id))
      outcome3.replies should contain only None
    }

    "find schema by id" in withTestDriver { driver =>
      val cmd = CreateSchema("id", "name", Some("description"), "BPMN", "schema")
      val cmd2 = FindSchemaById("id")
      val outcome = driver.run(cmd)
      outcome.replies should contain only Schema("id", "name", Some("description"), "BPMN", "schema")
      outcome.events should contain only SchemaCreated(cmd.id, cmd.name, cmd.description, cmd.notation, cmd.schema)
      val outcome2 = driver.run(cmd2)
      outcome2.replies should contain only Some(Schema(cmd.id, cmd.name, cmd.description, cmd.notation, cmd.schema))
    }

    "find schema by non-existing id" in withTestDriver { driver =>
      val cmd2 = FindSchemaById("id")
      val outcome2 = driver.run(cmd2)
      outcome2.replies should contain only None
    }

  }
}
