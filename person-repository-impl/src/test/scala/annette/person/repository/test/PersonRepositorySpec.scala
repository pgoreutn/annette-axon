/*
 * Copyright 2018 Valery Lobachev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package annette.person.repository.test

import annette.person.repository.api.model.PersonFindQuery
import annette.person.repository.api.{PersonAlreadyExist, PersonNotFound, PersonRepositoryService}
import annette.person.repository.impl.PersonRepositoryApplication
import annette.shared.exceptions.AnnetteException
import com.lightbend.lagom.scaladsl.api.AdditionalConfiguration
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.Configuration

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}

class PersonRepositorySpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new PersonRepositoryApplication(ctx) with LocalServiceLocator {
      override def additionalConfiguration: AdditionalConfiguration = {
        super.additionalConfiguration ++ Configuration.from(
          Map(
            "cassandra-query-journal.eventual-consistency-delay" -> "0",
            "lagom.circuit-breaker.default.enabled" -> "off"
          )
        )
      }
    }
  }

  val client = server.serviceClient.implement[PersonRepositoryService]

  override protected def afterAll() = server.stop()

  "person repository service" should {

    "create person" in {
      val userPerson = TestData.userPerson()
      val contactPerson = TestData.contactPerson()
      for {
        createdUserPerson <- client.createPerson.invoke(userPerson)
        foundUserPerson <- client.getPersonById(userPerson.id, false).invoke()
        createdContactPerson <- client.createPerson.invoke(contactPerson)
        foundContactPerson <- client.getPersonById(contactPerson.id, false).invoke()
      } yield {
        createdUserPerson shouldBe userPerson.copy(updatedAt = createdUserPerson.updatedAt)
        foundUserPerson shouldBe userPerson.copy(updatedAt = createdUserPerson.updatedAt)
        createdContactPerson shouldBe contactPerson.copy(updatedAt = createdContactPerson.updatedAt)
        foundContactPerson shouldBe contactPerson.copy(updatedAt = createdContactPerson.updatedAt)
      }
    }

    "create person with existing person id" in {
      val userPerson = TestData.userPerson()
      for {
        createdUserPerson <- client.createPerson.invoke(userPerson)
        createdUserPerson2 <- client.createPerson.invoke(userPerson).recover {
          case th: Throwable => th
        }
      } yield {
        createdUserPerson shouldBe userPerson.copy(updatedAt = createdUserPerson.updatedAt)
        createdUserPerson2 shouldBe a[AnnetteException]
        createdUserPerson2.asInstanceOf[AnnetteException].code shouldBe PersonAlreadyExist.MessageCode
      }
    }

    "update person" in {
      val userPerson = TestData.userPerson()
      val contactPerson = TestData.contactPerson(userPerson.id)
      for {
        createdUserPerson <- client.createPerson.invoke(userPerson)
        foundUserPerson <- client.getPersonById(userPerson.id, false).invoke()
        createdContactPerson <- client.updatePerson.invoke(contactPerson)
        foundContactPerson <- client.getPersonById(contactPerson.id, false).invoke()
      } yield {
        createdUserPerson shouldBe userPerson.copy(updatedAt = createdUserPerson.updatedAt)
        foundUserPerson shouldBe userPerson.copy(updatedAt = createdUserPerson.updatedAt)
        createdContactPerson shouldBe contactPerson.copy(updatedAt = createdContactPerson.updatedAt)
        foundContactPerson shouldBe contactPerson.copy(updatedAt = createdContactPerson.updatedAt)
      }
    }

    "update person with non existing person id" in {
      val userPerson = TestData.userPerson()
      for {
        updatedUserPerson <- client.updatePerson.invoke(userPerson).recover {
          case th: Throwable => th
        }
      } yield {
        updatedUserPerson shouldBe a[AnnetteException]
        updatedUserPerson.asInstanceOf[AnnetteException].code shouldBe PersonNotFound.MessageCode
      }
    }

    "deactivate person" in {
      val userPerson = TestData.userPerson()
      for {
        createdUserPerson <- client.createPerson.invoke(userPerson)
        _ <- client.deactivatePerson(userPerson.id).invoke()
        deactivatedUserPerson <- client.getPersonById(userPerson.id, false).invoke()
      } yield {
        createdUserPerson shouldBe userPerson.copy(updatedAt = createdUserPerson.updatedAt)
        deactivatedUserPerson shouldBe userPerson.copy(updatedAt = deactivatedUserPerson.updatedAt, active = false)
      }
    }

    "deactivate person with non existing person id" in {
      val userPerson = TestData.userPerson()
      for {
        deactivatedUserPerson <- client.deactivatePerson(userPerson.id).invoke().recover {
          case th: Throwable => th
        }
      } yield {
        deactivatedUserPerson shouldBe a[AnnetteException]
        deactivatedUserPerson.asInstanceOf[AnnetteException].code shouldBe PersonNotFound.MessageCode
      }
    }

    "activate person" in {
      val userPerson = TestData.userPerson()
      for {
        createdUserPerson <- client.createPerson.invoke(userPerson)
        _ <- client.deactivatePerson(userPerson.id).invoke()
        deactivatedUserPerson <- client.getPersonById(userPerson.id, false).invoke()
        activatedUserPerson <- client.activatePerson(userPerson.id).invoke()
      } yield {
        createdUserPerson shouldBe userPerson.copy(updatedAt = createdUserPerson.updatedAt)
        deactivatedUserPerson shouldBe userPerson.copy(updatedAt = deactivatedUserPerson.updatedAt, active = false)
        activatedUserPerson shouldBe userPerson.copy(updatedAt = activatedUserPerson.updatedAt)
      }
    }

    "activate person with non existing person id" in {
      val userPerson = TestData.userPerson()
      for {
        activatedUserPerson <- client.activatePerson(userPerson.id).invoke().recover {
          case th: Throwable => th
        }
      } yield {
        activatedUserPerson shouldBe a[AnnetteException]
        activatedUserPerson.asInstanceOf[AnnetteException].code shouldBe PersonNotFound.MessageCode
      }
    }

    "import non existing person" in {
      val userPerson = TestData.userPerson()
      for {
        importedUserPerson <- client.importPerson.invoke(userPerson)
        foundUserPerson <- client.getPersonById(userPerson.id, false).invoke()
      } yield {
        importedUserPerson shouldBe userPerson.copy(updatedAt = importedUserPerson.updatedAt)
        foundUserPerson shouldBe userPerson.copy(updatedAt = importedUserPerson.updatedAt)
      }
    }

    "import existing person" in {
      val userPerson = TestData.userPerson()
      val contactPerson = TestData.contactPerson(userPerson.id)
      for {
        createdUserPerson <- client.createPerson.invoke(userPerson)
        foundUserPerson <- client.getPersonById(userPerson.id, false).invoke()
        importedContactPerson <- client.importPerson.invoke(contactPerson)
        foundContactPerson <- client.getPersonById(contactPerson.id, false).invoke()
      } yield {
        createdUserPerson shouldBe userPerson.copy(updatedAt = createdUserPerson.updatedAt)
        foundUserPerson shouldBe userPerson.copy(updatedAt = createdUserPerson.updatedAt)
        importedContactPerson shouldBe contactPerson.copy(updatedAt = importedContactPerson.updatedAt)
        foundContactPerson shouldBe contactPerson.copy(updatedAt = importedContactPerson.updatedAt)
      }
    }

    "import existing deactivated person" in {
      val userPerson = TestData.userPerson()
      val contactPerson = TestData.contactPerson(userPerson.id)
      for {
        createdUserPerson <- client.createPerson.invoke(userPerson)
        foundUserPerson <- client.getPersonById(userPerson.id, false).invoke()
        _ <- client.deactivatePerson(userPerson.id).invoke()
        deactivatedUserPerson <- client.getPersonById(userPerson.id, false).invoke()
        importedContactPerson <- client.importPerson.invoke(contactPerson)
        foundContactPerson <- client.getPersonById(contactPerson.id, false).invoke()
      } yield {
        createdUserPerson shouldBe userPerson.copy(updatedAt = createdUserPerson.updatedAt)
        foundUserPerson shouldBe userPerson.copy(updatedAt = createdUserPerson.updatedAt)
        deactivatedUserPerson shouldBe userPerson.copy(updatedAt = deactivatedUserPerson.updatedAt, active = false)
        importedContactPerson shouldBe contactPerson.copy(updatedAt = importedContactPerson.updatedAt)
        foundContactPerson shouldBe contactPerson.copy(updatedAt = importedContactPerson.updatedAt)
      }
    }

    "get person by id using readside" in {
      val userPerson = TestData.userPerson()
      (for {
        createdUserPerson <- client.createPerson.invoke(userPerson)
      } yield {
        awaitSuccess() {
          for {
            foundUserPerson <- client.getPersonById(userPerson.id).invoke()
          } yield {
            createdUserPerson shouldBe userPerson.copy(updatedAt = createdUserPerson.updatedAt)
            foundUserPerson shouldBe userPerson.copy(updatedAt = createdUserPerson.updatedAt)
          }
        }
      }).flatMap(identity)
    }

    "get persons by ids" in {
      val persons = (1 to 10).map(_ => TestData.userPerson())
      for {
        createdPersons <- Future.sequence(persons.map(person => client.createPerson.invoke(person)))
        foundPersons <- client.getPersonsByIds(false).invoke(createdPersons.map(_.id).toSet)
      } yield {
        foundPersons should contain theSameElementsAs createdPersons
      }
    }

    "get persons by ids using readside" in {
      val persons = (1 to 10).map(_ => TestData.userPerson())
      (for {
        createdPersons <- Future.sequence(persons.map(person => client.createPerson.invoke(person)))
      } yield {
        awaitSuccess() {
          for {
            foundPersons <- client.getPersonsByIds().invoke(createdPersons.map(_.id).toSet)
          } yield {
            foundPersons should contain theSameElementsAs createdPersons
          }
        }
      }).flatMap(identity)
    }

    "find person by filter" in {
      val toFindByLastname = TestData.userPerson(lastname = "Dostoevsky")
      val toFindByFirstname = TestData.userPerson(firstname = "Fedor")
      val toFindByMiddlename = TestData.userPerson(middlename = Some("Mikhailovich"))
      val toFindByPhone = TestData.userPerson(phone = Some("+7-999-888-7766"))
      val toFindByEmail = TestData.userPerson(email = Some("fmd@greatwriter.com"))
      val persons = (1 to 10).map(_ => TestData.userPerson()) ++
        Seq(toFindByLastname, toFindByEmail, toFindByFirstname, toFindByMiddlename, toFindByPhone)
      (for {
        createdPersons <- Future.sequence(persons.map(person => client.createPerson.invoke(person)))
      } yield {
        awaitSuccess() {
          for {
            foundPersons <- client.getPersonsByIds().invoke(createdPersons.map(_.id).toSet)
            foundByLastname <- client.findPersons.invoke(PersonFindQuery(size = 10, filter = Some(toFindByLastname.lastname.take(7))))
            foundByFirstname <- client.findPersons.invoke(PersonFindQuery(size = 10, filter = Some(toFindByFirstname.firstname.take(4))))
            foundByMiddlename <- client.findPersons.invoke(PersonFindQuery(size = 10, filter = toFindByMiddlename.middlename.map(_.take(7))))
            foundByPhone <- client.findPersons.invoke(PersonFindQuery(size = 10, filter = toFindByPhone.phone))
            foundByEmail <- client.findPersons.invoke(PersonFindQuery(size = 10, filter = toFindByEmail.email.map(_.take(7))))
          } yield {
            foundPersons should contain theSameElementsAs createdPersons
            foundByEmail.hits.map(_.id) should contain (toFindByEmail.id)
            foundByPhone.hits.map(_.id) should contain (toFindByPhone.id)
            foundByFirstname.hits.map(_.id) should contain (toFindByFirstname.id)
            foundByLastname.hits.map(_.id) should contain (toFindByLastname.id)
            foundByMiddlename.hits.map(_.id) should contain (toFindByMiddlename.id)
          }
        }
      }).flatMap(identity)
    }

    "find person by first name, last name, middle name, email, phone" in {
      val toFindByLastname = TestData.userPerson(lastname = "Dostoevsky")
      val toFindByFirstname = TestData.userPerson(firstname = "Fedor")
      val toFindByMiddlename = TestData.userPerson(middlename = Some("Mikhailovich"))
      val toFindByPhone = TestData.userPerson(phone = Some("+7-999-888-7766"))
      val toFindByEmail = TestData.userPerson(email = Some("fmd@greatwriter.com"))
      val persons = (1 to 10).map(_ => TestData.userPerson()) ++
        Seq(toFindByLastname, toFindByEmail, toFindByFirstname, toFindByMiddlename, toFindByPhone)
      (for {
        createdPersons <- Future.sequence(persons.map(person => client.createPerson.invoke(person)))
      } yield {
        awaitSuccess() {
          for {
            foundPersons <- client.getPersonsByIds().invoke(createdPersons.map(_.id).toSet)
            foundByLastname <- client.findPersons.invoke(PersonFindQuery(size = 10, lastname = Some(toFindByLastname.lastname.take(7))))
            foundByFirstname <- client.findPersons.invoke(PersonFindQuery(size = 10, firstname = Some(toFindByFirstname.firstname.take(4))))
            foundByMiddlename <- client.findPersons.invoke(PersonFindQuery(size = 10, middlename = toFindByMiddlename.middlename.map(_.take(7))))
            foundByPhone <- client.findPersons.invoke(PersonFindQuery(size = 10, phone = toFindByPhone.phone))
            foundByEmail <- client.findPersons.invoke(PersonFindQuery(size = 10, email = toFindByEmail.email.map(_.take(7))))
          } yield {
            foundPersons should contain theSameElementsAs createdPersons
            foundByEmail.hits.map(_.id) should contain (toFindByEmail.id)
            foundByPhone.hits.map(_.id) should contain (toFindByPhone.id)
            foundByFirstname.hits.map(_.id) should contain (toFindByFirstname.id)
            foundByLastname.hits.map(_.id) should contain (toFindByLastname.id)
            foundByMiddlename.hits.map(_.id) should contain (toFindByMiddlename.id)
          }
        }
      }).flatMap(identity)
    }

  }

  def awaitSuccess[T](maxDuration: FiniteDuration = 10.seconds, checkEvery: FiniteDuration = 200.milliseconds)(block: => Future[T]): Future[T] = {
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
