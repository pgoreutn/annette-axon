package axon.knowledge.repository.impl

import akka.Done
import annette.shared.exceptions.AnnetteException
import axon.knowledge.repository.api.{DataSchemaAlreadyExist, DataSchemaNotFound, KnowledgeRepositoryService}
import axon.knowledge.repository.api.model.{DataSchema, DataSchemaKey}
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

    "create dataSchema & find it by id" in {
      val dataSchema = TestData.addressDS()
      for {
        created <- client.createDataSchema.invoke(dataSchema)
        found <- client.findDataSchemaByKey(dataSchema.key).invoke()
      } yield {
        created shouldBe dataSchema
        found shouldBe dataSchema
      }
    }

    "create dataSchema with existing id" in {
      val dataSchema = TestData.addressDS()
      for {
        created <- client.createDataSchema.invoke(dataSchema)
        created2 <- client.createDataSchema
          .invoke(dataSchema)
          .recover {
            case th: Throwable => th
          }
      } yield {
        created shouldBe a[DataSchema]
        created2 shouldBe a[AnnetteException]
        created2.asInstanceOf[AnnetteException].code shouldBe DataSchemaAlreadyExist.MessageCode
      }

    }

    "update dataSchema" in {
      val sch = TestData.addressDS(name = "another name", description = "another description")
      for {
        created <- client.createDataSchema.invoke(TestData.addressDS(sch.key))
        updated <- client.updateDataSchema
          .invoke(sch)
          .recover { case th: Throwable => th }
        dataSchema <- client.findDataSchemaByKey(sch.key).invoke()
      } yield {
        created shouldBe a[DataSchema]
        updated shouldBe a[DataSchema]
        dataSchema shouldBe sch
      }

    }

    "update dataSchema with non-existing id" in {
      val sch = TestData.addressDS()
      for {
        updated <- client.updateDataSchema
          .invoke(sch)
          .recover { case th: Throwable => th }
      } yield {
        updated shouldBe a[AnnetteException]
        updated.asInstanceOf[AnnetteException].code shouldBe DataSchemaNotFound.MessageCode
      }

    }

    "delete dataSchema" in {
      val sch = TestData.addressDS()
      for {
        created <- client.createDataSchema.invoke(sch)
        found1 <- client
          .findDataSchemaByKey(sch.key)
          .invoke()
          .recover { case th: Throwable => th }
        deleted <- client.deleteDataSchema(sch.key).invoke()
        found2 <- client
          .findDataSchemaByKey(sch.key)
          .invoke()
          .recover {
            case th: Throwable =>
              th
          }
      } yield {
        created shouldBe a[DataSchema]
        found1 shouldBe sch
        deleted shouldBe Done
        found2 shouldBe a[AnnetteException]
        found2.asInstanceOf[AnnetteException].code shouldBe DataSchemaNotFound.MessageCode
      }

    }

    "delete dataSchema with nonexisting id" in {
      val key = TestData.addressDS().key
      for {
        deleted <- client
          .deleteDataSchema(key)
          .invoke()
          .recover { case th: Throwable => th }
      } yield {
        deleted shouldBe a[AnnetteException]
        deleted.asInstanceOf[AnnetteException].code shouldBe DataSchemaNotFound.MessageCode
      }

    }

    "find data schema by non-existing id" in {
      val xmlDataSchema = TestData.addressDS()
      for {
        found <- client
          .findDataSchemaByKey(Random.nextInt().toString)
          .invoke()
          .recover { case th: Throwable => th }
      } yield {
        found shouldBe a[AnnetteException]
        found.asInstanceOf[AnnetteException].code shouldBe DataSchemaNotFound.MessageCode
      }
    }

    "find dataSchemas" in {
      val keys = Seq(
        s"key-${Random.nextInt()}",
        s"key-${Random.nextInt()}",
        s"key-${Random.nextInt()}"
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
      val dataSchemas = for (i <- keys.indices) yield TestData.addressDS(keys(i), names(i), descriptions(i))

      val createFuture = Future.traverse(dataSchemas)(dataSchema => client.createDataSchema.invoke(dataSchema))

      (for {
        created <- createFuture.recover { case th: Throwable => th }
      } yield {
        awaitSuccess() {
          for {
            found0 <- client.findDataSchema.invoke("")
            found1 <- client.findDataSchema.invoke(keys(0))
            found2 <- client.findDataSchema.invoke(names(1))
            found3 <- client.findDataSchema.invoke(descriptions(2))
            found4 <- client.findDataSchema.invoke(Random.nextInt().toString)
          } yield {

            found0.length shouldBe >=(keys.length)

            found1.length shouldBe 1
            found1.head.key shouldBe keys(0)
            found1.head.name shouldBe names(0)
            found1.head.description shouldBe Some(descriptions(0))

            found2.length shouldBe 1
            found2.head.key shouldBe keys(1)
            found2.head.name shouldBe names(1)
            found2.head.description shouldBe Some(descriptions(1))

            found3.length shouldBe 1
            found3.head.key shouldBe keys(2)
            found3.head.name shouldBe names(2)
            found3.head.description shouldBe Some(descriptions(2))

            found4.length shouldBe 0

          }
        }
      }).flatMap(identity)
    }

    "find data schema by keys" in {
      import scala.collection.immutable.Seq

      val keys = Seq(
        s"key-${Random.nextInt()}",
        s"key-${Random.nextInt()}",
        s"key-${Random.nextInt()}"
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
      val dataSchemas = for (i <- keys.indices) yield TestData.addressDS(keys(i), names(i), descriptions(i))

      val createFuture = Future.traverse(dataSchemas)(dataSchema => client.createDataSchema.invoke(dataSchema))

      (for {
        _ <- createFuture.recover { case th: Throwable => th }
      } yield {
        awaitSuccess() {
          for {
            found1 <- client.findDataSchemaByKeys.invoke(keys)
          } yield {

            found1.length shouldBe 3
            // found1 should contain d

          }
        }
      }).flatMap(identity)
    }

  }

  def awaitSuccess[T](maxDuration: FiniteDuration = 10.seconds, checkEvery: FiniteDuration = 200.milliseconds)(block: => Future[T]): Future[T] = {
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
