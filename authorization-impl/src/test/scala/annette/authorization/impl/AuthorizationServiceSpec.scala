package annette.authorization.impl

import akka.Done
import annette.authorization.api._
import annette.authorization.api.model.{AuthorizationPrincipal, CheckPermissions, FindPermissions, Permission, PrincipalAssignment, Role, RoleFilter, RoleId}
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
        yield Role(ids(i), names(i), Some(descriptions(i)), Set(Permission("p1"), Permission("p2"), Permission("p3")))

      val createFuture = Future.traverse(roles)(role => client.createRole.invoke(role))

      (for {
        created <- createFuture.recover { case th: Throwable => th }
      } yield {
        awaitSuccess() {
          for {
            found0 <- client.findRoles.invoke(RoleFilter(0, 1000, Some("")))
            found2 <- client.findRoles.invoke(RoleFilter(0, 1000, Some(names(1))))
            found4 <- client.findRoles.invoke(RoleFilter(0, 1000, Some(Random.nextInt().toString)))
          } yield {

            (found0.total >= ids.length) shouldBe true

            found2.total shouldBe 1
            found2.hits.head.id shouldBe ids(1)
            found2.hits.head.data.get.name shouldBe names(1)
            found2.hits.head.data.get.description shouldBe Some(descriptions(1))

            found4.total shouldBe 0
          }
        }
      }).flatMap(identity)
    }

    "assign principal" in {

      val role1 = s"role-${Random.nextInt(99999)}"
      val role2 = s"role-${Random.nextInt(99999)}"

      val user1 = AuthorizationPrincipal("user", s"user-${Random.nextInt(99999)}")

      (for {
        assignment1 <- client.assignPrincipal.invoke(PrincipalAssignment(user1, role1))
        assignment2 <- client.assignPrincipal.invoke(PrincipalAssignment(user1, role2))
      } yield {
        awaitSuccess() {
          for {
            principals1 <- client.findPrincipalsAssignedToRole(role1).invoke()
            principals2 <- client.findPrincipalsAssignedToRole(role2).invoke()
            roles <- client.findRolesAssignedToPrincipal.invoke(user1)
          } yield {
            assignment1 shouldBe Done
            assignment2 shouldBe Done
            principals1 should have size 1
            principals2 should have size 1
            principals1 should contain(user1)
            principals2 should contain(user1)
            roles should have size 2
            roles should contain (role1)
            roles should contain (role2)
          }
        }
      }).flatMap(identity)
    }

    "unassign principal" in {

      val role1 = s"role-${Random.nextInt(99999)}"
      val role2 = s"role-${Random.nextInt(99999)}"

      val user1 = AuthorizationPrincipal("user", s"user-${Random.nextInt(99999)}")

      (for {
        assignment1 <- client.assignPrincipal.invoke(PrincipalAssignment(user1, role1))
        assignment2 <- client.assignPrincipal.invoke(PrincipalAssignment(user1, role2))
        assignment3 <- client.unassignPrincipal.invoke(PrincipalAssignment(user1, role1))
      } yield {
        awaitSuccess() {
          for {
            principals1 <- client.findPrincipalsAssignedToRole(role1).invoke()
            principals2 <- client.findPrincipalsAssignedToRole(role2).invoke()
            roles <- client.findRolesAssignedToPrincipal.invoke(user1)
          } yield {
            assignment1 shouldBe Done
            assignment2 shouldBe Done
            assignment3 shouldBe Done
            principals1 should have size 0
            principals2 should have size 1
            principals1 shouldNot contain(user1)
            principals2 should contain(user1)
            roles should have size 1
            roles shouldNot contain (role1)
            roles should contain (role2)
          }
        }
      }).flatMap(identity)
    }

    "findRolesAssignedToPrincipal & findPrincipalsAssignedToRole" in {
      val role1 = s"roleA-1-${Random.nextInt(99999)}"
      val role2 = s"roleA-1-${Random.nextInt(99999)}"
      val role3 = s"roleA-1-${Random.nextInt(99999)}"

      val user1 = AuthorizationPrincipal("user", s"user-${Random.nextInt(99999)}")
      val user2 = AuthorizationPrincipal("user", s"user-${Random.nextInt(99999)}")
      val user3 = AuthorizationPrincipal("user", s"user-${Random.nextInt(99999)}")

      (for {
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user1, role1))
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user1, role2))
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user1, role3))
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user2, role1))
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user2, role2))
      } yield {
        awaitSuccess() {
          for {
            roles1 <- client.findRolesAssignedToPrincipal.invoke(user1)
            roles2 <- client.findRolesAssignedToPrincipal.invoke(user2)
            roles3 <- client.findRolesAssignedToPrincipal.invoke(user3)
            principals1 <- client.findPrincipalsAssignedToRole(role1).invoke()
            principals2 <- client.findPrincipalsAssignedToRole(role2).invoke()
            principals3 <- client.findPrincipalsAssignedToRole(role3).invoke()
          } yield {
            roles1 should have size 3
            roles1 should contain allOf (role1, role2, role3)
            roles2 should have size 2
            roles2 should contain allOf (role1, role2)
            roles3 should have size 0
            principals1 should have size 2
            principals1 should contain allOf (user1, user2)
            principals2 should have size 2
            principals2 should contain allOf (user1, user2)
            principals3 should have size 1
            principals3 should contain (user1)
          }
        }
      }).flatMap(identity)
    }
    "checkAllPermissions" in {
      val P = Vector(
        Permission(s"Permission-A-${Random.nextInt(99999)}", "a", "b", "c"),
        Permission(s"Permission-A-${Random.nextInt(99999)}", "a", "b", ""),
        Permission(s"Permission-A-${Random.nextInt(99999)}", "a", "", ""),
        Permission(s"Permission-B-${Random.nextInt(99999)}", "a", "b", "c"),
        Permission(s"Permission-B-${Random.nextInt(99999)}", "a", "", ""),
        Permission(s"Permission-B-${Random.nextInt(99999)}", "", "b", "c")
      )
      val role1 = Role(s"roleA-1-${Random.nextInt(99999)}", "name", None, Set(P(0), P(1)))
      val role2 = Role(s"roleA-2-${Random.nextInt(99999)}", "name", None, Set(P(2), P(3)))
      val role3 = Role(s"roleA-3-${Random.nextInt(99999)}", "name", None, Set(P(4), P(5)))

      val user1 = AuthorizationPrincipal("user", s"user-${Random.nextInt(99999)}")
      val user2 = AuthorizationPrincipal("user", s"user-${Random.nextInt(99999)}")
      val user3 = AuthorizationPrincipal("user", s"user-${Random.nextInt(99999)}")

      (for {
        created1 <- client.createRole.invoke(role1)
        created2 <- client.createRole.invoke(role2)
        created3 <- client.createRole.invoke(role3)
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user1, role1.id))
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user1, role2.id))
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user1, role3.id))
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user2, role1.id))
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user2, role2.id))
      } yield {
        awaitSuccess() {
          for {
            check1 <- client.checkAllPermissions.invoke(
              CheckPermissions(
                Set(user1),
                Set(P(0), P(1), P(2), P(3), P(4), P(5))
              )
            )
            check2 <- client.checkAllPermissions.invoke(
              CheckPermissions(
                Set(user2),
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
                Set(user1),
                Set()
              )
            )
            check5 <- client.checkAllPermissions.invoke(
              CheckPermissions(
                Set(user1),
                Set(P(0), Permission("not-existent"))
              )
            )
          } yield {
            println(s"check1 = $check1")
            println(s"check2 = $check2")
            println(s"check3 = $check3")
            println(s"check4 = $check4")
            println(s"check5 = $check5")

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
        Permission(s"Permission-A-${Random.nextInt(99999)}", "a", "b", "c"),
        Permission(s"Permission-A-${Random.nextInt(99999)}", "a", "b", ""),
        Permission(s"Permission-A-${Random.nextInt(99999)}", "a", "", ""),
        Permission(s"Permission-B-${Random.nextInt(99999)}", "a", "b", "c"),
        Permission(s"Permission-B-${Random.nextInt(99999)}", "a", "", ""),
        Permission(s"Permission-B-${Random.nextInt(99999)}", "", "b", "c")
      )
      val role1 = Role(s"roleA-1-${Random.nextInt(99999)}", "name", None, Set(P(0), P(1)))
      val role2 = Role(s"roleA-2-${Random.nextInt(99999)}", "name", None, Set(P(2), P(3)))
      val role3 = Role(s"roleA-3-${Random.nextInt(99999)}", "name", None, Set(P(4), P(5)))

      val user1 = AuthorizationPrincipal("user", s"user-${Random.nextInt(99999)}")
      val user2 = AuthorizationPrincipal("user", s"user-${Random.nextInt(99999)}")
      val user3 = AuthorizationPrincipal("user", s"user-${Random.nextInt(99999)}")

      (for {
        created1 <- client.createRole.invoke(role1)
        created2 <- client.createRole.invoke(role2)
        created3 <- client.createRole.invoke(role3)
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user1, role1.id))
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user1, role2.id))
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user1, role3.id))
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user2, role1.id))
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user2, role2.id))
      } yield {
        awaitSuccess() {

          for {
            check1 <- client.checkAnyPermissions.invoke(
              CheckPermissions(
                Set(user1),
                Set(P(0), Permission("not-existent"), Permission("not-existent2"))
              )
            )
            check2 <- client.checkAnyPermissions.invoke(
              CheckPermissions(
                Set(user2),
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
                Set(user1),
                Set()
              )
            )
            check5 <- client.checkAnyPermissions.invoke(
              CheckPermissions(
                Set(user2),
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
      val role1 = Role(s"roleA-1-${Random.nextInt(99999)}", "name", None, Set(P(0), P(1)))
      val role2 = Role(s"roleA-2-${Random.nextInt(99999)}", "name", None, Set(P(2), P(3)))
      val role3 = Role(s"roleA-3-${Random.nextInt(99999)}", "name", None, Set(P(4), P(5)))

      val user1 = AuthorizationPrincipal("user", s"user-${Random.nextInt(99999)}")
      val user2 = AuthorizationPrincipal("user", s"user-${Random.nextInt(99999)}")
      val user3 = AuthorizationPrincipal("user", s"user-${Random.nextInt(99999)}")

      (for {
        created1 <- client.createRole.invoke(role1)
        created2 <- client.createRole.invoke(role2)
        created3 <- client.createRole.invoke(role3)
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user1, role1.id))
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user1, role2.id))
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user1, role3.id))
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user2, role2.id))
        _ <- client.assignPrincipal.invoke(PrincipalAssignment(user2, role3.id))
      } yield {
        awaitSuccess() {

          for {
            check1 <- client.findPermissions.invoke(
              FindPermissions(
                Set(user1),
                Set(P(0).id)
              )
            )
            check2 <- client.findPermissions.invoke(
              FindPermissions(
                Set(user1),
                Set(P(0).id, P(5).id)
              )
            )
            check3 <- client.findPermissions.invoke(
              FindPermissions(
                Set(user2),
                Set(P(0).id)
              )
            )
            check4 <- client.findPermissions.invoke(
              FindPermissions(
                Set(user2),
                Set(P(0).id, P(5).id)
              )
            )
            check5 <- client.findPermissions.invoke(
              FindPermissions(
                Set(user1),
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
                Set(user1),
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
   /* "user to role assignment/unassignment" in {
      val assignments = Set(
        "user1" -> "role1",
        "user1" -> "role2",
        "user2" -> "role1",
        "user2" -> "role4",
        "user3" -> "role1",
        "user3" -> "role2",
        "user3" -> "role4"
      )

      val rolesByUser = assignments.groupBy(a => a._1).map(r => r._1 -> r._2.map(_._2))
      val usersByRoles = assignments.groupBy(a => a._2).map(r => r._1 -> r._2.map(_._1))

      for {
        _ <- Future.traverse(rolesByUser.keys) { key =>
          client
            .assignUserToRoles(key)
            .invoke(rolesByUser(key))
        }
        //.recover { case th: Throwable => th }
        userRes <- Future
          .traverse(rolesByUser.keys) { key =>
            client
              .findRolesAssignedToUser(key)
              .invoke()
              .map(r => key -> r)
          }
          .map(_.toMap)
        //.recover { case th: Throwable => th }
        roleRes <- Future
          .traverse(usersByRoles.keys) { key =>
            client
              .findUsersAssignedToRole(key)
              .invoke()
              .map(r => key -> r)
          }
          .map(_.toMap)
        //.recover { case th: Throwable => th }
        _ <- Future.traverse(rolesByUser.keys) { key =>
          client
            .unassignUserFromRoles(key)
            .invoke(rolesByUser(key))
        }
        userRes2 <- Future
          .traverse(rolesByUser.keys) { key =>
            client
              .findRolesAssignedToUser(key)
              .invoke()
              .map(r => key -> r)
          }
          .map(_.toMap)
        //.recover { case th: Throwable => th }
        roleRes2 <- Future
          .traverse(usersByRoles.keys) { key =>
            client
              .findUsersAssignedToRole(key)
              .invoke()
              .map(r => key -> r)
          }
          .map(_.toMap)
      } yield {
        userRes shouldBe rolesByUser
        roleRes shouldBe usersByRoles
        userRes2 shouldBe rolesByUser.keys.map(k => k -> Set.empty[RoleId]).toMap
        roleRes2 shouldBe usersByRoles.keys.map(k => k -> Set.empty[UserId]).toMap
      }

    }

    */
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
