package annette.authorization.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import annette.authorization.api.model.{Permission, Role}
import annette.authorization.impl.role.{CreateRole, DeleteRole, FindRoleById, RoleCommand, RoleCreated, RoleDeleted, RoleEntity, RoleEvent, RoleUpdated, UpdateRole}
import annette.shared.exceptions.AnnetteTransportException
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class RoleEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val system = ActorSystem("RoleEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(AuthorizationSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def withTestDriver(block: PersistentEntityTestDriver[RoleCommand, RoleEvent, Option[Role]] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new RoleEntity, "bpm-1")
    block(driver)
    // driver.getAllIssues should have size 0
  }

  "role entity" should {

    "create role" in withTestDriver { driver =>
      val role = Role("id", "name", Some("description"), Set(Permission("p1"), Permission("p2"), Permission("p3")))
      val cmd = CreateRole(role)
      val outcome = driver.run(cmd)
      outcome.replies should contain only role
      outcome.events should contain only RoleCreated(role)
    }

    "create role with existing id" in withTestDriver { driver =>
      val role = Role("id", "name", Some("description"), Set(Permission("p1"), Permission("p2"), Permission("p3")))
      val cmd = CreateRole(role)
      val outcome = driver.run(cmd)
      outcome.replies should contain only role
      outcome.events should contain only RoleCreated(role)
      val outcome2 = driver.run(cmd)
      outcome2.events shouldBe empty
      outcome2.replies should have size 1
      outcome2.replies.head shouldBe a[AnnetteTransportException]
    }

    "update role" in withTestDriver { driver =>
      val role = Role("id", "name", Some("description"), Set(Permission("p1"), Permission("p2"), Permission("p3")))
      val cmd = CreateRole(role)
      val role2 = role.copy(name = "name2", permissions = Set(Permission("p1"), Permission("p4"), Permission("p5")))
      val cmd2 = UpdateRole(role2)
      val outcome = driver.run(cmd)
      outcome.replies should contain only role
      outcome.events should contain only RoleCreated(role)
      val outcome2 = driver.run(cmd2)
      outcome2.replies should contain only role2
      outcome2.events should contain only RoleUpdated(role2)
    }

    "update role with non-existing id" in withTestDriver { driver =>
      val role = Role("id", "name", Some("description"), Set(Permission("p1"), Permission("p2"), Permission("p3")))
      val cmd = UpdateRole(role)
      val outcome2 = driver.run(cmd)
      outcome2.events shouldBe empty
      outcome2.replies should have size 1
      outcome2.replies.head shouldBe a[AnnetteTransportException]
    }

    "delete role" in withTestDriver { driver =>
      val role = Role("id", "name", Some("description"), Set(Permission("p1"), Permission("p2"), Permission("p3")))
      val cmd = CreateRole(role)
      val outcome = driver.run(cmd)
      outcome.replies should contain only role
      outcome.events should contain only RoleCreated(role)
      val outcome2 = driver.run(DeleteRole(role.id))
      outcome2.replies should contain only Done
      outcome2.events should contain only RoleDeleted(role.id)
      val outcome3 = driver.run(FindRoleById(role.id))
      outcome3.replies should contain only None
    }

    "find role by id" in withTestDriver { driver =>
      val role = Role("id", "name", Some("description"), Set(Permission("p1"), Permission("p2"), Permission("p3")))
      val cmd = CreateRole(role)
      val cmd2 = FindRoleById("id")
      val outcome = driver.run(cmd)
      outcome.replies should contain only role
      outcome.events should contain only RoleCreated(role)
      val outcome2 = driver.run(cmd2)
      outcome2.replies should contain only Some(role)
    }

    "find role by non-existing id" in withTestDriver { driver =>
      val cmd2 = FindRoleById("id")
      val outcome2 = driver.run(cmd2)
      outcome2.replies should contain only None
    }

  }
}
