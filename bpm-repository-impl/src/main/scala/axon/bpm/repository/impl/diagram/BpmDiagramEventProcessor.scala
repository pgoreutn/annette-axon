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

package axon.bpm.repository.impl.diagram

import akka.Done
import com.datastax.driver.core.PreparedStatement
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}

import scala.concurrent.{ExecutionContext, Future}

private[impl] class BpmDiagramEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext)
    extends ReadSideProcessor[BpmDiagramEvent] {
  private var insertBpmDiagramStatement: PreparedStatement = null
  private var updateBpmDiagramStatement: PreparedStatement = null
  private var deleteBpmDiagramStatement: PreparedStatement = null

  def buildHandler = {
    readSide
      .builder[BpmDiagramEvent]("bpmDiagramEventOffset")
      .setGlobalPrepare(createTables)
      .setPrepare(_ => prepareStatements())
      .setEventHandler[BpmDiagramCreated](e => insertBpmDiagram(e.event))
      .setEventHandler[BpmDiagramUpdated](e => updateBpmDiagram(e.event))
      .setEventHandler[BpmDiagramDeleted](e => deleteBpmDiagram(e.event))
      .build
  }

  def aggregateTags = BpmDiagramEvent.Tag.allTags

  private def createTables() = {
    for {
      _ <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS bpmDiagrams (
          id text PRIMARY KEY,
          name text,
          description text,
          notation text,
          process_definitions text
        )
      """)

    } yield Done
  }

  private def prepareStatements() = {
    for {
      insertBpmDiagram <- session.prepare("""
        INSERT INTO bpmDiagrams(id, name, description, notation, process_definitions) VALUES (?, ?, ?, ?, ?)
      """)
      updateBpmDiagram <- session.prepare("""
        UPDATE bpmDiagrams SET name = ?, description = ?, process_definitions = ? WHERE id = ?
      """)
      deleteBpmDiagram <- session.prepare("""
        DELETE FROM bpmDiagrams WHERE id = ?
      """)
    } yield {
      insertBpmDiagramStatement = insertBpmDiagram
      updateBpmDiagramStatement = updateBpmDiagram
      deleteBpmDiagramStatement = deleteBpmDiagram
      Done
    }
  }

  private def insertBpmDiagram(event: BpmDiagramCreated) = {
    println(s"insertBpmDiagram: ${event.bpmDiagram.id}")
    Future.successful(
      List(
        insertBpmDiagramStatement.bind(
          event.bpmDiagram.id,
          event.bpmDiagram.name,
          event.bpmDiagram.description.getOrElse(""),
          event.bpmDiagram.notation,
          event.bpmDiagram.processDefinitions.getOrElse("")
        )
      ))
  }

  private def updateBpmDiagram(event: BpmDiagramUpdated) = {
    println(s"updateBpmDiagram: ${event.bpmDiagram.id}")

    Future.successful(
      List(
        updateBpmDiagramStatement
          .bind(event.bpmDiagram.name, event.bpmDiagram.description.getOrElse(""), event.bpmDiagram.processDefinitions.getOrElse(""), event.bpmDiagram.id)
      ))
  }

  private def deleteBpmDiagram(event: BpmDiagramDeleted) = {
    println(s"deleteBpmDiagram: ${event.id}")

    Future.successful(
      List(
        deleteBpmDiagramStatement.bind(event.id)
      ))
  }

}
