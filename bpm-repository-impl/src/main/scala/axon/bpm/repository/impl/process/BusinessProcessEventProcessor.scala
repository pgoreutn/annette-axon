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

package axon.bpm.repository.impl.process

import akka.Done
import axon.bpm.repository.api.model.{ProcessReferenceById, ProcessReferenceByKey}
import com.datastax.driver.core.PreparedStatement
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}

import scala.concurrent.{ExecutionContext, Future}

private[impl] class BusinessProcessEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext)
    extends ReadSideProcessor[BusinessProcessEvent] {
  private var insertBusinessProcessStatement: PreparedStatement = null
  private var updateBusinessProcessStatement: PreparedStatement = null
  private var deleteBusinessProcessStatement: PreparedStatement = null

  def buildHandler = {
    readSide
      .builder[BusinessProcessEvent]("businessProcessEventOffset")
      .setGlobalPrepare(createTables)
      .setPrepare(_ => prepareStatements())
      .setEventHandler[BusinessProcessCreated](e => insertBusinessProcess(e.event))
      .setEventHandler[BusinessProcessUpdated](e => updateBusinessProcess(e.event))
      .setEventHandler[BusinessProcessDeleted](e => deleteBusinessProcess(e.event))
      .build
  }

  def aggregateTags = BusinessProcessEvent.Tag.allTags

  private def createTables() = {
    for {
      _ <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS business_processes (
          key text PRIMARY KEY,
          name text,
          description text,
          ref_type text,
          process_reference text,
          data_schema_key text
        )
      """)

    } yield Done
  }

  private def prepareStatements() = {
    for {
      insertBusinessProcess <- session.prepare("""
        INSERT INTO business_processes(key, name, description, ref_type, process_reference, data_schema_key)
          VALUES (:key, :name, :description, :refType, :processReference, :dataSchemaKey)
      """)
      updateBusinessProcess <- session.prepare("""
        UPDATE business_processes SET name = :name, description = :description,
          ref_type = :refType, process_reference = :processReference, data_schema_key = :dataSchemaKey WHERE key = :key
      """)
      deleteBusinessProcess <- session.prepare("""
        DELETE FROM business_processes WHERE key = :key
      """)
    } yield {
      insertBusinessProcessStatement = insertBusinessProcess
      updateBusinessProcessStatement = updateBusinessProcess
      deleteBusinessProcessStatement = deleteBusinessProcess
      Done
    }
  }

  private def insertBusinessProcess(event: BusinessProcessCreated) = {
    val (refType, processReference) = event.businessProcess.processReference match {
      case ProcessReferenceByKey(key) => "key" -> key
      case ProcessReferenceById(id)   => "id" -> id
    }
    Future.successful(
      List(
        insertBusinessProcessStatement
          .bind()
          .setString("key", event.businessProcess.key)
          .setString("name", event.businessProcess.name)
          .setString("description", event.businessProcess.description.getOrElse(""))
          .setString("refType", refType)
          .setString("processReference", processReference)
          .setString("dataSchemaKey", event.businessProcess.dataSchemaKey)
      ))
  }

  private def updateBusinessProcess(event: BusinessProcessUpdated) = {
    val (refType, processReference) = event.businessProcess.processReference match {
      case ProcessReferenceByKey(key) => "key" -> key
      case ProcessReferenceById(id)   => "id" -> id
    }
    Future.successful(
      List(
        updateBusinessProcessStatement
          .bind()
          .setString("key", event.businessProcess.key)
          .setString("name", event.businessProcess.name)
          .setString("description", event.businessProcess.description.getOrElse(""))
          .setString("refType", refType)
          .setString("processReference", processReference)
          .setString("dataSchemaKey", event.businessProcess.dataSchemaKey)
      ))
  }

  private def deleteBusinessProcess(event: BusinessProcessDeleted) = {
    println(s"deleteBusinessProcess: ${event.key}")

    Future.successful(
      List(
        deleteBusinessProcessStatement.bind().setString("key", event.key)
      ))
  }

}
