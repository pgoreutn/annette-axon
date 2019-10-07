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

package annette.authorization.impl.assignment

import akka.Done
import annette.authorization.api.model.PrincipalAssignment
import com.datastax.driver.core.PreparedStatement
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import org.slf4j.LoggerFactory
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

private[impl] class AssignmentEventProcessor(
    session: CassandraSession,
    readSide: CassandraReadSide,
    val configuration: Configuration
)(implicit val ec: ExecutionContext)
    extends ReadSideProcessor[AssignmentEvent] {
  private val log = LoggerFactory.getLogger(classOf[AssignmentEventProcessor])

  private var insertRoleToPrincipalAssignmentStatement: PreparedStatement = null
  private var insertPrincipalToRoleAssignmentStatement: PreparedStatement = null
  private var deleteRoleToPrincipalAssignmentStatement: PreparedStatement = null
  private var deletePrincipalToRoleAssignmentStatement: PreparedStatement = null

  def buildHandler = {
    readSide
      .builder[AssignmentEvent]("assignmentEventOffset")
      .setGlobalPrepare(globalPrepare)
      .setPrepare(_ => prepareStatements())
      .setEventHandler[PrincipalAssigned](e => assignPrincipal(e.event.assignment))
      .setEventHandler[PrincipalUnassigned](e => unassignPrincipal(e.event.assignment))
      .build
  }

  def aggregateTags = AssignmentEvent.Tag.allTags

  private def globalPrepare() = {
    for {
      _ <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS principal_to_role (
          principal_type text,
          principal_id text,
          role_id text,
          PRIMARY KEY ( (principal_type, principal_id), role_id)
        )
      """)
      _ <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS role_to_principal (
          role_id text,
          principal_type text,
          principal_id text,
          PRIMARY KEY ( role_id, principal_type, principal_id)
        )
      """)

    } yield {
      Done
    }
  }

  private def prepareStatements() = {

    for {
      insertRoleToPrincipalAssignment <- session.prepare("""
        INSERT INTO role_to_principal( principal_type, principal_id, role_id ) VALUES ( :principal_type, :principal_id, :role_id )
      """
      )
      insertPrincipalToRoleAssignment <- session.prepare("""
        INSERT INTO principal_to_role( principal_type, principal_id, role_id  ) VALUES ( :principal_type, :principal_id, :role_id )
      """
      )
      deleteRoleToPrincipalAssignment <- session.prepare("""
        DELETE FROM role_to_principal WHERE  role_id  = :role_id and principal_type = :principal_type and principal_id = :principal_id
      """
      )
      deletePrincipalToRoleAssignment <- session.prepare("""
        DELETE FROM principal_to_role WHERE  principal_type = :principal_type and principal_id = :principal_id  and role_id  = :role_id
      """
      )
    } yield {
      insertRoleToPrincipalAssignmentStatement = insertRoleToPrincipalAssignment
      insertPrincipalToRoleAssignmentStatement = insertPrincipalToRoleAssignment
      deleteRoleToPrincipalAssignmentStatement = deleteRoleToPrincipalAssignment
      deletePrincipalToRoleAssignmentStatement = deletePrincipalToRoleAssignment
      Done
    }
  }

  private def assignPrincipal(assignment: PrincipalAssignment) = {
    Future.successful(
      List(
        bind(insertRoleToPrincipalAssignmentStatement, assignment),
        bind(insertPrincipalToRoleAssignmentStatement, assignment),
      )
    )
  }

  private def unassignPrincipal(assignment: PrincipalAssignment) = {
    Future.successful(
      List(
        bind(deleteRoleToPrincipalAssignmentStatement, assignment),
        bind(deletePrincipalToRoleAssignmentStatement, assignment)
      )
    )
  }

  private def bind(statement: PreparedStatement, assignment: PrincipalAssignment) = {
    statement
      .bind()
      .setString("principal_type", assignment.principal.principalType)
      .setString("principal_id", assignment.principal.principalId)
      .setString("role_id", assignment.roleId),
  }

}
