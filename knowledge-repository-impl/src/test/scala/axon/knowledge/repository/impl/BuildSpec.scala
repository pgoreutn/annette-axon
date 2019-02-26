package axon.knowledge.repository.impl

import axon.knowledge.repository.api.KnowledgeRepositoryService
import com.lightbend.lagom.scaladsl.api.AdditionalConfiguration
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.Configuration

import scala.concurrent.Future

class BuildSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

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

  val personDS = TestData.person
  val addressDS = TestData.addressDS("Address")

  var ready: Future[Any] = null

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    val schemas = Seq(personDS, addressDS, TestData.createdBase, TestData.updatedBase, TestData.versionBase)
    ready = Future.traverse(schemas) { schema =>
      client.createDataSchema.invoke(schema)
    }
    //Await.ready(future, Duration.Inf)
  }

  override protected def afterAll() = server.stop()

  "knowledge repository build" should {

    "build one level data struct def" in {

      for {
        _ <- ready
        oneLevelAddress <- client.buildSingleLevel(addressDS.key).invoke()
      } yield {
        println(s"\n\n\nbuild onelevel data struct def\n\n${oneLevelAddress.prettyPrint()}\n\n\n")
        oneLevelAddress.fields.values.foreach(println)

        oneLevelAddress.fields.size shouldBe 7
      }

    }

    "build multi level data struct def" in {

      for {
        _ <- ready
        oneLevelDS <- client.buildMultiLevel(personDS.key).invoke()
      } yield {
        println(s"\n\n\nbuild multilevel data struct def\n${oneLevelDS.prettyPrint()}\n\n\n")
        oneLevelDS.fields.values.foreach(println)

        oneLevelDS.fields.size shouldBe 10

      }

    }

//    "build JsObject" in {
//           for {
//        jsObject <- builder.buildJsObject(TestData.person.key)
//      } yield {
//        println
//        println("build JsObject")
//        println(Json.prettyPrint(jsObject))
//
//        1 + 1 shouldBe 2
//      }
//    }

  }

}
