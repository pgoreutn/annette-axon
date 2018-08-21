package axon.bpm.repository.impl

import akka.Done
import axon.bpm.repository.api.{BpmRepositoryService, Schema}
import com.lightbend.lagom.scaladsl.api.transport.{BadRequest, NotFound, TransportException}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.InvalidCommandException
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class BpmRepositoryServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx => new BpmRepositoryApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[BpmRepositoryService]

  override protected def afterAll() = server.stop()

  "bpm repository service" should {

    "create schema" in {
      val xmlSchema = TestData.xmlSchema()
      for {
        created <- client.createSchema("BPMN").invoke(xmlSchema)
        found <- client.findSchemaById(TestData.id).invoke()
      } yield {
        created shouldBe Done
        found shouldBe Schema(TestData.id, TestData.name, Some(TestData.description), "BPMN", xmlSchema)
      }
    }

    "create schema with existing id" in {
      for {
        created <- client.createSchema("BPMN").invoke(TestData.xmlSchema("id3"))
        created2 <- client
          .createSchema("BPMN")
          .invoke(TestData.xmlSchema("id3"))
          .recover {
            case th: Throwable =>
              println(th)
              th
          }
      } yield {
        println(created2)
        created shouldBe Done
        created2 shouldBe a[BadRequest] //("bpmRepository.schema.alreadyExist")
        created2.asInstanceOf[BadRequest].exceptionMessage.detail shouldBe "bpmRepository.schema.alreadyExist"
      }

    }

    /*"update schema" in {
      val cmd = CreateSchema("id", "name", Some("description"), "BPMN", "schema")
      val cmd2 = UpdateSchema("id", "name1", Some("description1"), "schema")
      val outcome = driver.run(cmd)
      outcome.replies should contain only Done
      outcome.events should contain only SchemaCreated(cmd.id, cmd.name, cmd.description, cmd.notation, cmd.schema)
      val outcome2 = driver.run(cmd2)
      outcome2.replies should contain only Done
      outcome2.events should contain only SchemaUpdated(cmd2.id, cmd2.name, cmd2.description, cmd2.schema)
    }

    "update schema with non-existing id" in {
      val cmd2 = UpdateSchema("id", "name1", Some("description1"), "schema")
      val outcome2 = driver.run(cmd2)
      outcome2.events shouldBe empty
      outcome2.replies should have size 1
      outcome2.replies.head shouldBe a[InvalidCommandException]
    }

    "find schema by id" in {
      val cmd = CreateSchema("id", "name", Some("description"), "BPMN", "schema")
      val cmd2 = FindSchemaById("id")
      val outcome = driver.run(cmd)
      outcome.replies should contain only Done
      outcome.events should contain only SchemaCreated(cmd.id, cmd.name, cmd.description, cmd.notation, cmd.schema)
      val outcome2 = driver.run(cmd2)
      outcome2.replies should contain only Some(Schema(cmd.id, cmd.name, cmd.description, cmd.notation, cmd.schema))
    }

    "find schema by non-existing id" in {
      val cmd2 = FindSchemaById("id")
      val outcome2 = driver.run(cmd2)
      outcome2.replies should contain only None
    }*/

  }

}
