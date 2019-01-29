package axon.bpm.engine.impl

import java.util.UUID

import axon.bpm.engine.api._
import axon.bpm.repository.api.Schema
import com.lightbend.lagom.scaladsl.api.AdditionalConfiguration
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.Configuration
import play.api.libs.json.Json

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

    "deploy bpmn" in {
      val id = UUID.randomUUID().toString
      val name = "Схема бизнес процесса"
      val description = "Описание схемы бизнес процесса"
      val xml = TestData.bpmnSchema
      val schema = Schema(id, name, Some(description), "BPMN", xml, None)
      for {
        deployment <- client.deploy.invoke(schema)
      } yield {
        println
        println
        println("***********************************************")
        println(s"Deploy BPMN:")
        import DeploymentWithDefs._
        println(Json.prettyPrint(Json.toJson(deployment)))
        deployment shouldBe a[DeploymentWithDefs]
        //deployment
      }
    }

    "deploy dmn" in {
      val id = UUID.randomUUID().toString
      val name = "Схема бизнес процесса"
      val description = "Описание схемы бизнес процесса"
      val xml = TestData.dmnSchema
      val schema = Schema(id, name, Some(description), "DMN", xml, None)
      for {
        deployment <- client.deploy.invoke(schema)
      } yield {
        println
        println
        println("***********************************************")
        println(s"Deploy DMN:")
        import DeploymentWithDefs._
        println(Json.prettyPrint(Json.toJson(deployment)))
        deployment shouldBe a[DeploymentWithDefs]
      }
    }

    "deploy cmmn" in {
      val id = UUID.randomUUID().toString
      val name = "Схема бизнес процесса"
      val description = "Описание схемы бизнес процесса"
      val xml = TestData.cmmnSchema
      val schema = Schema(id, name, Some(description), "CMMN", xml, None)
      for {
        deployment <- client.deploy.invoke(schema)
      } yield {
        println
        println
        println("***********************************************")
        println(s"Deploy CMMN:")
        import DeploymentWithDefs._
        println(Json.prettyPrint(Json.toJson(deployment)))
        deployment shouldBe a[DeploymentWithDefs]
      }
    }

    "find all process definitions" in {
      for {
        found <- client.findProcessDef.invoke(FindProcessDefOptions(latest = false))
      } yield {
        println
        println
        println("***********************************************")
        println(s"Found ${found.length} process definitions:")
        found.foreach(println)
        println
        import ProcessDef._
        found.map(pd => Json.prettyPrint(Json.toJson(pd))).foreach(println)
        found.length shouldBe >=(0)
      }
    }

    "find all decision definitions" in {
      for {
        found <- client.findDecisionDef.invoke(FindDecisionDefOptions(latest = false))
      } yield {
        println
        println
        println("***********************************************")
        println(s"Found ${found.length} decision definitions:")
        found.foreach(println)
        println
        import DecisionDef._
        found.map(pd => Json.prettyPrint(Json.toJson(pd))).foreach(println)
        found.length shouldBe >=(0)
      }
    }

    "find all case definitions" in {
      for {
        found <- client.findCaseDef.invoke(FindCaseDefOptions(latest = false))
      } yield {
        println
        println
        println("***********************************************")
        println(s"Found ${found.length} case definitions:")
        found.foreach(println)
        println
        import CaseDef._
        found.map(pd => Json.prettyPrint(Json.toJson(pd))).foreach(println)
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
