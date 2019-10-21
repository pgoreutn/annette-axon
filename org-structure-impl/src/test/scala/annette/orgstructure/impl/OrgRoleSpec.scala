package annette.orgstructure.impl

import java.time.OffsetDateTime

import akka.Done
import annette.orgstructure.api._
import annette.orgstructure.api.model.{OrgRole, OrgRoleFilter}
import annette.shared.exceptions.AnnetteException
import com.lightbend.lagom.scaladsl.api.AdditionalConfiguration
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.Configuration

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.util.Random

class OrgRoleSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new OrgStructureApplication(ctx) with LocalServiceLocator {
      override def additionalConfiguration: AdditionalConfiguration = {
        super.additionalConfiguration ++ Configuration.from(
          Map(
            "cassandra-query-journal.eventual-consistency-delay" -> "2s",
            "lagom.circuit-breaker.default.enabled" -> "off",
            "elastic.url" -> "https://localhost:9200",
            "elastic.prefix" -> "test",
            "elastic.username" -> "admin",
            "elastic.password" -> "admin",
            "elastic.allowInsecure" -> "true"
          ))
      }
    }
  }

  val client = server.serviceClient.implement[OrgStructureService]

  override protected def afterAll() = server.stop()

  "OrgRole operations" should {

    "create role & find it by id" in {
      val role = OrgRole("id", "name", Some("description"), OffsetDateTime.now())
      for {
        created <- client.createOrgRole.invoke(role)
        found <- client.getOrgRoleById(role.id, false).invoke()
      } yield {
        created shouldBe role
        found shouldBe role
      }
    }

    "create role with existing id" in {
      val role = OrgRole("id2", "name", Some("description"), OffsetDateTime.now())
      for {
        created <- client.createOrgRole.invoke(role)
        created2 <- client.createOrgRole
          .invoke(role)
          .recover {
            case th: Throwable => th
          }
      } yield {
        created shouldBe role
        created2 shouldBe a[AnnetteException]
        created2.asInstanceOf[AnnetteException].code shouldBe OrgRoleAlreadyExist.MessageCode
      }

    }

    "update role" in {
      val role = OrgRole("id3", "name", Some("description"), OffsetDateTime.now())
      val role2 = OrgRole("id3", "name1", Some("description1"), OffsetDateTime.now())
      for {
        created <- client.createOrgRole.invoke(role)
        updated <- client.updateOrgRole
          .invoke(role2)
          .recover { case th: Throwable => th }
        found <- client.getOrgRoleById(role.id, false).invoke()
      } yield {
        created shouldBe role
        updated shouldBe role2
        found shouldBe role2
      }

    }

    "update role with non-existing id" in {
      val role = OrgRole("id4", "name", Some("description"), OffsetDateTime.now())
      for {
        updated <- client.updateOrgRole
          .invoke(role)
          .recover { case th: Throwable => th }
      } yield {
        updated shouldBe a[AnnetteException]
        updated.asInstanceOf[AnnetteException].code shouldBe OrgRoleNotFound.MessageCode
      }

    }

    "deactivate role" in {
      val id = s"id${Random.nextInt()}"
      val role = OrgRole(id, "name", Some("description"), OffsetDateTime.now())
      for {
        created <- client.createOrgRole.invoke(role)
        found1 <- client
          .getOrgRoleById(id, false)
          .invoke()
          .recover { case th: Throwable => th }
        deleted <- client.deactivateOrgRole(id).invoke()
        found2 <- client
          .getOrgRoleById(id)
          .invoke()
          .recover {
            case th: Throwable =>
              th
          }
      } yield {
        created shouldBe role
        found1 shouldBe role
        deleted shouldBe Done
        found2 shouldBe a[AnnetteException]
        found2.asInstanceOf[AnnetteException].code shouldBe OrgRoleNotFound.MessageCode
      }

    }

    "deactivate role with nonexisting id" in {
      val id = s"id${Random.nextInt()}"
      for {
        deleted <- client
          .deactivateOrgRole(id)
          .invoke()
          .recover { case th: Throwable => th }
      } yield {
        deleted shouldBe a[AnnetteException]
        deleted.asInstanceOf[AnnetteException].code shouldBe OrgRoleNotFound.MessageCode
      }

    }

    "get role by non-existing id" in {
      val role = OrgRole("id5", "name", Some("description"), OffsetDateTime.now())
      for {
        found <- client
          .getOrgRoleById(Random.nextInt().toString, false)
          .invoke()
          .recover { case th: Throwable => th }
      } yield {
        found shouldBe a[AnnetteException]
        found.asInstanceOf[AnnetteException].code shouldBe OrgRoleNotFound.MessageCode
      }
    }

    "find roles" in {
      val ids = Seq(
        s"id-${Random.nextInt()}",
        s"id-${Random.nextInt()}",
        s"id-${Random.nextInt()}"
      )
      val names = Seq(
        s"наименование-${Random.nextInt()}",
        s"наименование-${Random.nextInt()}",
        s"наименование-${Random.nextInt()}"
      )
      val descriptions = Seq(
        s"description-${Random.nextInt()}",
        s"description-${Random.nextInt()}",
        s"description-${Random.nextInt()}"
      )
      val roles = for (i <- ids.indices)
        yield OrgRole(ids(i), names(i), Some(descriptions(i)), OffsetDateTime.now())

      val createFuture = Future.traverse(roles)(role => client.createOrgRole.invoke(role))

      (for {
        _ <- createFuture.recover { case th: Throwable => th }
      } yield {
        awaitSuccess() {
          for {
            found0 <- client.findOrgRoles.invoke(OrgRoleFilter(0, 1000, Some("")))
            found2 <- client.findOrgRoles.invoke(OrgRoleFilter(0, 1000, Some(names(1))))
            found4 <- client.findOrgRoles.invoke(OrgRoleFilter(0, 1000, Some(Random.nextInt().toString)))
          } yield {

            found0.total should be >= ids.length.toLong
            found2.total shouldBe 1
            found2.hits.head.id shouldBe ids(1)
            found4.total shouldBe 0
          }
        }
      }).flatMap(identity)
    }

   
  }
  def awaitSuccess[T](maxDuration: FiniteDuration = 15.seconds, checkEvery: FiniteDuration = 500.milliseconds)(block: => Future[T]): Future[T] = {
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
