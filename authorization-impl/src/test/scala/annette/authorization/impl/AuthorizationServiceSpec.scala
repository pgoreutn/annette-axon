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

    "checkAllPermissions" in {
      val P = Vector(
        Permission("Permission-A", "a", "b", "c"),
        Permission("Permission-A", "a", "b", ""),
        Permission("Permission-A", "a", "", ""),
        Permission("Permission-B", "a", "b", "c"),
        Permission("Permission-B", "a", "", ""),
        Permission("Permission-B", "", "b", "c")
      )
      val role1 = Role("roleA-1", "name", None, Set(P(0), P(1)))
      val role2 = Role("roleA-2", "name", None, Set(P(2), P(3)))
      val role3 = Role("roleA-3", "name", None, Set(P(4), P(5)))

      (for {
        created1 <- client.createRole.invoke(role1)
        created2 <- client.createRole.invoke(role2)
        created3 <- client.createRole.invoke(role3)
      } yield {
        awaitSuccess() {

          for {
            check1 <- client.checkAllPermissions.invoke(
              CheckPermissions(
                Set(role1.id, role2.id, role3.id),
                Set(P(0), P(1), P(2), P(3), P(4), P(5))
              )
            )
            check2 <- client.checkAllPermissions.invoke(
              CheckPermissions(
                Set(role1.id, role2.id),
                Set(P(0), P(1), P(2), P(3), P(4), P(5))
              )
            )
            check3 <- client.checkAllPermissions.invoke(
              CheckPermissions(
                Set(),
                Set(P(0), P(1), P(2), P(3), P(4), P(5))
              )
            )
            check4 <- client.checkAllPermissions.invoke(
              CheckPermissions(
                Set(role1.id, role2.id, role3.id),
                Set()
              )
            )
            check5 <- client.checkAllPermissions.invoke(
              CheckPermissions(
                Set(role1.id, role2.id, role3.id),
                Set(P(0), Permission("not-existent"))
              )
            )
          } yield {

            created1 shouldBe role1
            created2 shouldBe role2
            created3 shouldBe role3
            check1 shouldBe true
            check2 shouldBe false
            check3 shouldBe false
            check4 shouldBe false // ???
            check5 shouldBe false

          }
        }
      }).flatMap(identity)
    }

    "checkAnyPermissions" in {
      val P = Vector(
        Permission("Permission-C", "a", "b", "c"),
        Permission("Permission-C", "a", "b", ""),
        Permission("Permission-C", "a", "", ""),
        Permission("Permission-D", "a", "b", "c"),
        Permission("Permission-D", "a", "", ""),
        Permission("Permission-D", "", "b", "c")
      )
      val role1 = Role("roleB-1", "name", None, Set(P(0), P(1)))
      val role2 = Role("roleB-2", "name", None, Set(P(2), P(3)))
      val role3 = Role("roleB-3", "name", None, Set(P(4), P(5)))

      (for {
        created1 <- client.createRole.invoke(role1)
        created2 <- client.createRole.invoke(role2)
        created3 <- client.createRole.invoke(role3)
      } yield {
        awaitSuccess() {

          for {
            check1 <- client.checkAnyPermissions.invoke(
              CheckPermissions(
                Set(role1.id, role2.id, role3.id),
                Set(P(0), Permission("not-existent"), Permission("not-existent2"))
              )
            )
            check2 <- client.checkAnyPermissions.invoke(
              CheckPermissions(
                Set(role1.id, role2.id),
                Set(P(5), Permission("not-existent"), Permission("not-existent2"))
              )
            )
            check3 <- client.checkAnyPermissions.invoke(
              CheckPermissions(
                Set(),
                Set(P(0), P(1), P(2), P(3), P(4), P(5))
              )
            )
            check4 <- client.checkAnyPermissions.invoke(
              CheckPermissions(
                Set(role1.id, role2.id, role3.id),
                Set()
              )
            )
            check5 <- client.checkAnyPermissions.invoke(
              CheckPermissions(
                Set(role1.id, role2.id, role3.id),
                Set(Permission("not-existent"), Permission("not-existent2"), Permission("not-existent2"))
              )
            )
          } yield {

            created1 shouldBe role1
            created2 shouldBe role2
            created3 shouldBe role3
            check1 shouldBe true
            check2 shouldBe false
            check3 shouldBe false
            check4 shouldBe false // ???
            check5 shouldBe false

          }
        }
      }).flatMap(identity)
    }

    "findPermissions" in {
      val P = Vector(
        Permission("Permission-E", "a", "b", "c"),
        Permission("Permission-E", "a", "b", ""),
        Permission("Permission-E", "a", "", ""),
        Permission("Permission-F", "a", "b", "c"),
        Permission("Permission-F", "a", "", ""),
        Permission("Permission-F", "", "b", "c")
      )
      val role1 = Role("roleC-1", "name", None, Set(P(0), P(1)))
      val role2 = Role("roleC-2", "name", None, Set(P(2), P(3)))
      val role3 = Role("roleC-3", "name", None, Set(P(4), P(5)))

      (for {
        created1 <- client.createRole.invoke(role1)
        created2 <- client.createRole.invoke(role2)
        created3 <- client.createRole.invoke(role3)
      } yield {
        awaitSuccess() {

          for {
            check1 <- client.findPermissions.invoke(
              FindPermissions(
                Set(role1.id, role2.id, role3.id),
                Set(P(0).id)
              )
            )
            check2 <- client.findPermissions.invoke(
              FindPermissions(
                Set(role1.id, role2.id, role3.id),
                Set(P(0).id, P(5).id)
              )
            )
            check3 <- client.findPermissions.invoke(
              FindPermissions(
                Set(role2.id, role3.id),
                Set(P(0).id)
              )
            )
            check4 <- client.findPermissions.invoke(
              FindPermissions(
                Set(role2.id, role3.id),
                Set(P(0).id, P(5).id)
              )
            )
            check5 <- client.findPermissions.invoke(
              FindPermissions(
                Set(role1.id, role2.id, role3.id),
                Set("non-existent1", P(5).id)
              )
            )
            check6 <- client.findPermissions.invoke(
              FindPermissions(
                Set(),
                Set("non-existent1", P(5).id)
              )
            )
            check7 <- client.findPermissions.invoke(
              FindPermissions(
                Set(role1.id, role2.id, role3.id),
                Set()
              )
            )

          } yield {

            created1 shouldBe role1
            created2 shouldBe role2
            created3 shouldBe role3

            check1.size shouldBe 3
            check1 should contain(P(0))
            check1 should contain(P(1))
            check1 should contain(P(2))

            check2.size shouldBe 6
            check2 should contain(P(0))
            check2 should contain(P(1))
            check2 should contain(P(2))
            check2 should contain(P(3))
            check2 should contain(P(4))
            check2 should contain(P(5))

            check3.size shouldBe 1
            check3 should contain(P(2))

            check4.size shouldBe 4
            check4 should contain(P(2))
            check4 should contain(P(3))
            check4 should contain(P(4))
            check4 should contain(P(5))

            check5.size shouldBe 3
            check5 should contain(P(3))
            check5 should contain(P(4))
            check5 should contain(P(5))

            check6.size shouldBe 0

            check7.size shouldBe 0

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
