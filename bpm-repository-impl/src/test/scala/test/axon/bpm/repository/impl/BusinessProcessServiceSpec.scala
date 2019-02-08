package test.axon.bpm.repository.impl

import akka.Done
import annette.shared.exceptions.AnnetteException
import axon.bpm.repository.api.model.BusinessProcess
import axon.bpm.repository.api.{BpmRepositoryService, BusinessProcessAlreadyExist, BusinessProcessNotFound}
import axon.bpm.repository.impl.BpmRepositoryApplication
import com.lightbend.lagom.scaladsl.api.AdditionalConfiguration
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.Configuration

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.util.Random

class BusinessProcessServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new BpmRepositoryApplication(ctx) with LocalServiceLocator {
      override def additionalConfiguration: AdditionalConfiguration = {
        super.additionalConfiguration ++ Configuration.from(
          Map(
            "cassandra-query-journal.eventual-consistency-delay" -> "0"
          ))
      }
    }
  }

  val client = server.serviceClient.implement[BpmRepositoryService]

  override protected def afterAll() = server.stop()

  "bpm repository service" should {

    "create businessProcess & find it by id" in {
      val diagram = TestData.businessProcess()
      for {
        created <- client.createBusinessProcess.invoke(diagram)
        found <- client.findBusinessProcessById(diagram.id).invoke()
      } yield {
        created shouldBe diagram
        found shouldBe diagram
      }
    }

    "create businessProcess with existing id" in {
      for {
        created <- client.createBusinessProcess.invoke(TestData.businessProcess("id3"))
        created2 <- client.createBusinessProcess
          .invoke(TestData.businessProcess("id3"))
          .recover {
            case th: Throwable => th
          }
      } yield {
        created shouldBe a[BusinessProcess]
        created2 shouldBe a[AnnetteException]
        created2.asInstanceOf[AnnetteException].code shouldBe BusinessProcessAlreadyExist.MessageCode
      }
    }

    "update businessProcess" in {
      val sch = TestData.businessProcess("id4", name = "name1", description = "description1")
      for {
        created <- client.createBusinessProcess.invoke(TestData.businessProcess("id4"))
        updated <- client.updateBusinessProcess
          .invoke(sch)
          .recover { case th: Throwable => th }
        businessProcess <- client.findBusinessProcessById("id4").invoke()
      } yield {
        created shouldBe a[BusinessProcess]
        updated shouldBe a[BusinessProcess]
        businessProcess.name shouldBe "name1"
        businessProcess.description shouldBe Some("description1")
      }

    }

    "update businessProcess with non-existing id" in {
      val sch = TestData.businessProcess("id5", name = "name1", description = "description1")
      for {
        updated <- client.updateBusinessProcess
          .invoke(sch)
          .recover { case th: Throwable => th }
      } yield {
        updated shouldBe a[AnnetteException]
        updated.asInstanceOf[AnnetteException].code shouldBe BusinessProcessNotFound.MessageCode
      }

    }

    "delete businessProcess" in {
      val id = s"id${Random.nextInt()}"
      val diagram = TestData.businessProcess(id)
      for {
        created <- client.createBusinessProcess.invoke(diagram)
        found1 <- client
          .findBusinessProcessById(id)
          .invoke()
          .recover { case th: Throwable => th }
        deleted <- client.deleteBusinessProcess(id).invoke()
        found2 <- client
          .findBusinessProcessById(id)
          .invoke()
          .recover {
            case th: Throwable =>
              th
          }
      } yield {
        created shouldBe a[BusinessProcess]
        found1 shouldBe diagram
        deleted shouldBe Done
        found2 shouldBe a[AnnetteException]
        found2.asInstanceOf[AnnetteException].code shouldBe BusinessProcessNotFound.MessageCode
      }

    }

    "delete businessProcess with nonexisting id" in {
      val id = s"id${Random.nextInt()}"
      for {
        deleted <- client
          .deleteBusinessProcess(id)
          .invoke()
          .recover { case th: Throwable => th }
      } yield {
        deleted shouldBe a[AnnetteException]
        deleted.asInstanceOf[AnnetteException].code shouldBe BusinessProcessNotFound.MessageCode
      }

    }

    "find businessProcess by non-existing id" in {
      val diagram = TestData.businessProcess()
      for {
        found <- client
          .findBusinessProcessById(Random.nextInt().toString)
          .invoke()
          .recover { case th: Throwable => th }
      } yield {
        found shouldBe a[AnnetteException]
        found.asInstanceOf[AnnetteException].code shouldBe BusinessProcessNotFound.MessageCode
      }
    }

    "find businessProcesss" in {
      val ids = Seq(
        s"id-${Random.nextInt(99)}",
        s"id-${Random.nextInt(99)}",
        s"id-${Random.nextInt(99)}"
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
      val businessProcesss = for (i <- ids.indices) yield TestData.businessProcess(ids(i), names(i), descriptions(i))

      val createFuture = Future.traverse(businessProcesss)(businessProcess => client.createBusinessProcess.invoke(businessProcess))

      (for {
        created <- createFuture.recover { case th: Throwable => th }
      } yield {
        awaitSuccess() {
          println("created")
          println(created)
          for {
            found0 <- client.findBusinessProcesss.invoke("")
            found2 <- client.findBusinessProcesss.invoke(names(1))
            found3 <- client.findBusinessProcesss.invoke(descriptions(2))
            found4 <- client.findBusinessProcesss.invoke(Random.nextInt().toString)
          } yield {

            println("found0")
            println(found0)
            println("found2")
            println(found2)
            println("found3")
            println(found3)
            println("found4")
            println(found4)

            found0.length shouldBe >=(ids.length)

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

  def awaitSuccess[T](maxDuration: FiniteDuration = 20.seconds, checkEvery: FiniteDuration = 100.milliseconds)(block: => Future[T]): Future[T] = {
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
