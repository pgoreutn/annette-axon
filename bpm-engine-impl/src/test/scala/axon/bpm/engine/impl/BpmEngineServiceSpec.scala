package axon.bpm.engine.impl

import java.util.UUID

import axon.bpm.engine.api._
import axon.bpm.repository.api.Schema
import com.lightbend.lagom.scaladsl.api.AdditionalConfiguration
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.Configuration

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}

class BpmEngineServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new BpmEngineApplication(ctx) with LocalServiceLocator {
      override def additionalConfiguration: AdditionalConfiguration = {
        super.additionalConfiguration ++ Configuration.from(
          Map(
            "cassandra-query-journal.eventual-consistency-delay" -> "0"
          ))
      }
    }
  }

  val client = server.serviceClient.implement[BpmEngineService]

  override protected def afterAll() = server.stop()

  "bpm engine service" should {

    "deploy" in {
      val id = UUID.randomUUID().toString
      val name = "Схема бизнес процесса"
      val description = "Описание схемы бизнес процесса"
      val xml = TestData.xmlSchema("Process_A", name, description)
      val schema = Schema(id, name, Some(description), "BPMN", xml, None)
      for {
        deployment <- client.deploy.invoke(schema)
      } yield {
        println(deployment)
        deployment shouldBe a[DeploymentWithDefs]
      }
    }

    "find all process definitions" in {
      for {
        found <- client.findProcessDef.invoke(FindProcessDefOptions(latest = false))
      } yield {
        println(s"Found ${found.length} items:")
        found.foreach(println)
        found.length shouldBe >=(0)
      }
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
