package axon.knowledge.repository.api

import axon.knowledge.repository.api.builder.DataStructBuilder
import axon.knowledge.repository.api.model.DataSchema
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.libs.json.Json

class DataSchemaSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  "data struct def " should {

    "build onelevel data struct def" in {
      val repo = DataStructRepo(
        Seq(TestData.addressDS, TestData.createdBase, TestData.updatedBase, TestData.versionBase).map(e => e.key -> e).toMap
      )

      val builder = new DataStructBuilder(repo)

      for {
        oneLevelAddress <- builder.buildSingleLevelDef(TestData.addressDS.key)
      } yield {
        println
        println("build onelevel data struct def")
        println(oneLevelAddress.prettyPrint())
        oneLevelAddress.fields.values.foreach(println)

        oneLevelAddress.fields.size shouldBe 7
      }

    }

    "build multilevel data struct def" in {
      val repo = DataStructRepo(
        Seq(TestData.person, TestData.addressDS, TestData.createdBase, TestData.updatedBase, TestData.versionBase).map(e => e.key -> e).toMap
      )

      val builder = new DataStructBuilder(repo)

      for {
        oneLevelDS <- builder.buildMultiLevelDef(TestData.person.key)
      } yield {
        println
        println("build multilevel data struct def")
        println(oneLevelDS.prettyPrint())
        println
        oneLevelDS.fields.values.foreach(println)

        oneLevelDS.fields.size shouldBe 10

      }

    }

    "build JsObject" in {
      val repo = DataStructRepo(
        Seq(TestData.person, TestData.addressDS, TestData.createdBase, TestData.updatedBase, TestData.versionBase).map(e => e.key -> e).toMap
      )

      val builder = new DataStructBuilder(repo)

      for {
        jsObject <- builder.buildJsObject(TestData.person.key)
      } yield {
        println
        println("build JsObject")
        println(Json.prettyPrint(jsObject))

        1 + 1 shouldBe 2
      }

    }

    "serialize" in {
      val seq = Seq(TestData.person, TestData.addressDS, TestData.createdBase, TestData.updatedBase, TestData.versionBase)
      (for {
        in <- seq
      } yield {
        val js = Json.toJson(in)
        //println(Json.prettyPrint(js))

        val out = Json.fromJson[DataSchema](js).getOrElse(sys.error("Oh no!"))
        out shouldBe in
      }).head

    }

    "serialize multilevel data struct def" in {
      val repo = DataStructRepo(
        Seq(TestData.person, TestData.addressDS, TestData.createdBase, TestData.updatedBase, TestData.versionBase).map(e => e.key -> e).toMap
      )

      val builder = new DataStructBuilder(repo)

      for {
        oneLevelDS <- builder.buildSingleLevelDef(TestData.person.key)
      } yield {

        val js = Json.toJson(oneLevelDS)
//        println
//        println("serialize multilevel data struct def")
//        println(Json.prettyPrint(js))

        val out = Json.fromJson[DataSchema](js).getOrElse(sys.error("Oh no!"))
        out shouldBe oneLevelDS
      }

    }

  }

}
