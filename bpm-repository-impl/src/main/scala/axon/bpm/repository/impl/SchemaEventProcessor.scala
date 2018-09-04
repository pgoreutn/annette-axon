package axon.bpm.repository.impl
import akka.Done
import com.datastax.driver.core.PreparedStatement
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}

import scala.concurrent.{ExecutionContext, Future}

private[impl] class SchemaEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext)
    extends ReadSideProcessor[SchemaEvent] {
  private var insertSchemaStatement: PreparedStatement = null
  private var updateSchemaStatement: PreparedStatement = null
  private var deleteSchemaStatement: PreparedStatement = null

  def buildHandler = {
    readSide
      .builder[SchemaEvent]("schemaEventOffset")
      .setGlobalPrepare(createTables)
      .setPrepare(_ => prepareStatements())
      .setEventHandler[SchemaCreated](e => insertSchema(e.event))
      .setEventHandler[SchemaUpdated](e => updateSchema(e.event))
      .setEventHandler[SchemaDeleted](e => deleteSchema(e.event))
      .build
  }

  def aggregateTags = SchemaEvent.Tag.allTags

  private def createTables() = {
    for {
      _ <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS schemas (
          id text PRIMARY KEY,
          name text,
          description text,
          notation text
        )
      """)

    } yield Done
  }

  private def prepareStatements() = {
    for {
      insertSchema <- session.prepare("""
        INSERT INTO schemas(id, name, description, notation) VALUES (?, ?, ?, ?)
      """)
      updateSchema <- session.prepare("""
        UPDATE schemas SET name = ?, description = ? WHERE id = ?
      """)
      deleteSchema <- session.prepare("""
        DELETE FROM schemas WHERE id = ?
      """)
    } yield {
      insertSchemaStatement = insertSchema
      updateSchemaStatement = updateSchema
      deleteSchemaStatement = deleteSchema
      Done
    }
  }

  private def insertSchema(event: SchemaCreated) = {
    println(s"insertSchema: ${event.id}")
    Future.successful(
      List(
        insertSchemaStatement.bind(event.id, event.name, event.description.getOrElse(""), event.notation)
      ))
  }

  private def updateSchema(event: SchemaUpdated) = {
    println(s"updateSchema: ${event.id}")

    Future.successful(
      List(
        updateSchemaStatement.bind(event.name, event.description.getOrElse(""), event.id)
      ))
  }

  private def deleteSchema(event: SchemaDeleted) = {
    println(s"deleteSchema: ${event.id}")

    Future.successful(
      List(
        deleteSchemaStatement.bind(event.id)
      ))
  }

}
