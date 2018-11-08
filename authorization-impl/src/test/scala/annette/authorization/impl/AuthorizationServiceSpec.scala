package annette.authorization.impl

import akka.Done
import annette.authorization.api._
import annette.shared.exceptions.AnnetteException
import com.lightbend.lagom.scaladsl.api.AdditionalConfiguration
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.Configuration

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.util.Random

class AuthorizationServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new AuthorizationApplication(ctx) with LocalServiceLocator {
      override def additionalConfiguration: AdditionalConfiguration = {
        super.additionalConfiguration ++ Configuration.from(
          Map(
            "cassandra-query-journal.eventual-consistency-delay" -> "0"
          ))
      }
    }
  }

  val client = server.serviceClient.implement[AuthorizationService]

  override protected def afterAll() = server.stop()

  "authorization service" should {

    "create role & find it by id" in {
      val role = Role("id", "name", Some("description"), Set(Permission("p1"), Permission("p2"), Permission("p3")))
      for {
        created <- client.createRole.invoke(role)
        found <- client.findRoleById(role.id).invoke()
      } yield {
        created shouldBe role
        found shouldBe role
      }
    }

    "create role with existing id" in {
      val role = Role("id2", "name", Some("description"), Set(Permission("p1"), Permission("p2"), Permission("p3")))
      for {
        created <- client.createRole.invoke(role)
        created2 <- client.createRole
          .invoke(role)
          .recover {
            case th: Throwable => th
          }
      } yield {
        created shouldBe role
        created2 shouldBe a[AnnetteException]
        created2.asInstanceOf[AnnetteException].code shouldBe RoleAlreadyExist.MessageCode
      }

    }

    "update role" in {
      val role = Role("id3", "name", Some("description"), Set(Permission("p1"), Permission("p2"), Permission("p3")))
      val role2 = Role("id3", "name1", Some("description1"), Set(Permission("p1"), Permission("p4"), Permission("p5")))
      for {
        created <- client.createRole.invoke(role)
        updated <- client.updateRole
          .invoke(role2)
          .recover { case th: Throwable => th }
        found <- client.findRoleById(role.id).invoke()
      } yield {
        created shouldBe role
        updated shouldBe role2
        found shouldBe role2
      }

    }

    "update role with non-existing id" in {
      val role = Role("id4", "name", Some("description"), Set(Permission("p1"), Permission("p2"), Permission("p3")))
      for {
        updated <- client.updateRole
          .invoke(role)
          .recover { case th: Throwable => th }
      } yield {
        updated shouldBe a[AnnetteException]
        updated.asInstanceOf[AnnetteException].code shouldBe RoleNotFound.MessageCode
      }

    }

    "delete role" in {
      val id = s"id${Random.nextInt()}"
      val role = Role(id, "name", Some("description"), Set(Permission("p1"), Permission("p2"), Permission("p3")))
      for {
        created <- client.createRole.invoke(role)
        found1 <- client
          .findRoleById(id)
          .invoke()
          .recover { case th: Throwable => th }
        deleted <- client.deleteRole(id).invoke()
        found2 <- client
          .findRoleById(id)
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
        found2.asInstanceOf[AnnetteException].code shouldBe RoleNotFound.MessageCode
      }

    }

    "delete role with nonexisting id" in {
      val id = s"id${Random.nextInt()}"
      for {
        deleted <- client
          .deleteRole(id)
          .invoke()
          .recover { case th: Throwable => th }
      } yield {
        deleted shouldBe a[AnnetteException]
        deleted.asInstanceOf[AnnetteException].code shouldBe RoleNotFound.MessageCode
      }

    }

    "find role by non-existing id" in {
      val role = Role("id5", "name", Some("description"), Set(Permission("p1"), Permission("p2"), Permission("p3")))
      for {
        found <- client
          .findRoleById(Random.nextInt().toString)
          .invoke()
          .recover { case th: Throwable => th }
      } yield {
        found shouldBe a[AnnetteException]
        found.asInstanceOf[AnnetteException].code shouldBe RoleNotFound.MessageCode
      }
    }

    "find roles" in {
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
      val roles = for (i <- 0 until ids.length)
        yield Role(ids(i), names(i), Some(descriptions(i)), Set(Permission("p1"), Permission("p2"), Permission("p3")))

      val createFuture = Future.traverse(roles)(role => client.createRole.invoke(role))

      (for {
        created <- createFuture.recover { case th: Throwable => th }
      } yield {
        awaitSuccess() {
          for {
            found0 <- client.findRoles.invoke("")
            found1 <- client.findRoles.invoke(ids(0))
            found2 <- client.findRoles.invoke(names(1))
            found3 <- client.findRoles.invoke(descriptions(2))
            found4 <- client.findRoles.invoke(Random.nextInt().toString)
          } yield {

            found0.size shouldBe >=(ids.length)

            found1.size shouldBe 1
            found1.head.id shouldBe ids(0)
            found1.head.name shouldBe names(0)
            found1.head.description shouldBe Some(descriptions(0))

            found2.size shouldBe 1
            found2.head.id shouldBe ids(1)
            found2.head.name shouldBe names(1)
            found2.head.description shouldBe Some(descriptions(1))

            found3.size shouldBe 1
            found3.head.id shouldBe ids(2)
            found3.head.name shouldBe names(2)
            found3.head.description shouldBe Some(descriptions(2))

            found4.size shouldBe 0

          }
        }
      }).flatMap(identity)
    }

  }

  /*def awaitSuccess[T](maxDuration: FiniteDuration = 10.seconds, checkEvery: FiniteDuration = 100.milliseconds)(block: => Future[T]): Future[T] = {
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
  }*/

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
