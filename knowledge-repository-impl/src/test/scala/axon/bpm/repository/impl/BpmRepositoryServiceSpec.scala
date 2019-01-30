package axon.knowledge.repository.impl

import akka.Done
import annette.shared.exceptions.AnnetteException
import axon.knowledge.repository.api.{KnowledgeRepositoryService, Schema, SchemaAlreadyExist, SchemaNotFound}
import com.lightbend.lagom.scaladsl.api.AdditionalConfiguration
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.Configuration

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.util.Random

class KnowledgeRepositoryServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new KnowledgeRepositoryApplication(ctx) with LocalServiceLocator {
      override def additionalConfiguration: AdditionalConfiguration = {
        super.additionalConfiguration ++ Configuration.from(
          Map(
            "cassandra-query-journal.eventual-consistency-delay" -> "0"
          ))
      }
    }
  }

  val client = server.serviceClient.implement[KnowledgeRepositoryService]

  override protected def afterAll() = server.stop()

  "knowledge repository service" should {

    "create schema & find it by id" in {
      val xmlSchema = TestData.xmlSchema()
      for {
        created <- client.createSchema.invoke(xmlSchema)
        found <- client.findSchemaById(TestData.id).invoke()
      } yield {
        created shouldBe Schema(TestData.id, TestData.name, Some(TestData.description), "KNOWLEDGEN", xmlSchema)
        found shouldBe Schema(TestData.id, TestData.name, Some(TestData.description), "KNOWLEDGEN", xmlSchema)
      }
    }

    "create schema with existing id" in {
      for {
        created <- client.createSchema.invoke(TestData.xmlSchema("id3"))
        created2 <- client.createSchema
          .invoke(TestData.xmlSchema("id3"))
          .recover {
            case th: Throwable => th
          }
      } yield {
        created shouldBe a[Schema]
        created2 shouldBe a[AnnetteException]
        created2.asInstanceOf[AnnetteException].code shouldBe SchemaAlreadyExist.MessageCode
      }

    }

    "update schema" in {
      val sch = TestData.xmlSchema("id4", name = "name1", description = "description1")
      for {
        created <- client.createSchema.invoke(TestData.xmlSchema("id4"))
        updated <- client.updateSchema
          .invoke(sch)
          .recover { case th: Throwable => th }
        schema <- client.findSchemaById("id4").invoke()
      } yield {
        created shouldBe a[Schema]
        updated shouldBe a[Schema]
        schema.xml shouldBe sch
        schema.name shouldBe "name1"
        schema.description shouldBe Some("description1")
      }

    }

    "update schema with non-existing id" in {
      val sch = TestData.xmlSchema("id5", name = "name1", description = "description1")
      for {
        updated <- client.updateSchema
          .invoke(sch)
          .recover { case th: Throwable => th }
      } yield {
        updated shouldBe a[AnnetteException]
        updated.asInstanceOf[AnnetteException].code shouldBe SchemaNotFound.MessageCode
      }

    }

    "delete schema" in {
      val id = s"id${Random.nextInt()}"
      val sch = TestData.xmlSchema(id)
      for {
        created <- client.createSchema.invoke(sch)
        found1 <- client
          .findSchemaById(id)
          .invoke()
          .recover { case th: Throwable => th }
        deleted <- client.deleteSchema(id).invoke()
        found2 <- client
          .findSchemaById(id)
          .invoke()
          .recover {
            case th: Throwable =>
              th
          }
      } yield {
        created shouldBe a[Schema]
        found1 shouldBe Schema(id, TestData.name, Some(TestData.description), "KNOWLEDGEN", sch)
        deleted shouldBe Done
        found2 shouldBe a[AnnetteException]
        found2.asInstanceOf[AnnetteException].code shouldBe SchemaNotFound.MessageCode
      }

    }

    "delete schema with nonexisting id" in {
      val id = s"id${Random.nextInt()}"
      for {
        deleted <- client
          .deleteSchema(id)
          .invoke()
          .recover { case th: Throwable => th }
      } yield {
        deleted shouldBe a[AnnetteException]
        deleted.asInstanceOf[AnnetteException].code shouldBe SchemaNotFound.MessageCode
      }

    }

    "find schema by non-existing id" in {
      val xmlSchema = TestData.xmlSchema()
      for {
        found <- client
          .findSchemaById(Random.nextInt().toString)
          .invoke()
          .recover { case th: Throwable => th }
      } yield {
        found shouldBe a[AnnetteException]
        found.asInstanceOf[AnnetteException].code shouldBe SchemaNotFound.MessageCode
      }
    }

    "find schemas" in {
      val ids = Seq(
        s"id-${Random.nextInt()}",
        s"id-${Random.nextInt()}",
        s"id-${Random.nextInt()}"
      )
      val names = Seq(
        s"name-${Random.nextInt()}",
        s"name-${Random.nextInt()}",
        s"name-${Random.nextInt()}"
      )
      val descriptions = Seq(
        s"description-${Random.nextInt()}",
        s"description-${Random.nextInt()}",
        s"description-${Random.nextInt()}"
      )
      val schemas = for (i <- 0 until ids.length) yield TestData.xmlSchema(ids(i), names(i), descriptions(i))

      val createFuture = Future.traverse(schemas)(schema => client.createSchema.invoke(schema))

      (for {
        created <- createFuture.recover { case th: Throwable => th }
      } yield {
        awaitSuccess() {
          for {
            found0 <- client.findSchemas.invoke("")
            found1 <- client.findSchemas.invoke(ids(0))
            found2 <- client.findSchemas.invoke(names(1))
            found3 <- client.findSchemas.invoke(descriptions(2))
            found4 <- client.findSchemas.invoke(Random.nextInt().toString)
          } yield {

            found0.length shouldBe >=(ids.length)

            found1.length shouldBe 1
            found1.head.id shouldBe ids(0)
            found1.head.name shouldBe names(0)
            found1.head.description shouldBe Some(descriptions(0))

            found2.length shouldBe 1
            found2.head.id shouldBe ids(1)
            found2.head.name shouldBe names(1)
            found2.head.description shouldBe Some(descriptions(1))

            found3.length shouldBe 1
            found3.head.id shouldBe ids(2)
            found3.head.name shouldBe names(2)
            found3.head.description shouldBe Some(descriptions(2))

            found4.length shouldBe 0

          }
        }
      }).flatMap(identity)
    }

  }

  def awaitSuccess[T](maxDuration: FiniteDuration = 10.seconds, checkEvery: FiniteDuration = 100.milliseconds)(block: => Future[T]): Future[T] = {
    val checkUntil = System.currentTimeMillis() + maxDuration.toMillis

    def doCheck(): Future[T] = {
      block.recoverWith {
        case recheck if checkUntil > System.currentTimeMillis() =>
          val timeout = Promise[T]()
          server.application.actorSystem.scheduler.scheduleOnce(checkEvery) {
            timeout.completeWith(doCheck())
          }(server.executionContext)
          timeout.future
      }
    }

    doCheck()
  }

}
