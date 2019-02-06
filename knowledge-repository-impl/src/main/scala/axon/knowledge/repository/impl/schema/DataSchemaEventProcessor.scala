package axon.knowledge.repository.impl.schema

import akka.Done
import com.datastax.driver.core.PreparedStatement
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}

import scala.concurrent.{ExecutionContext, Future}

private[impl] class DataSchemaEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext)
    extends ReadSideProcessor[DataSchemaEvent] {
  private var insertDataSchemaStatement: PreparedStatement = null
  private var updateDataSchemaStatement: PreparedStatement = null
  private var deleteDataSchemaStatement: PreparedStatement = null

  def buildHandler = {
    readSide
      .builder[DataSchemaEvent]("dataSchemaEventOffset")
      .setGlobalPrepare(createTables)
      .setPrepare(_ => prepareStatements())
      .setEventHandler[DataSchemaCreated](e => insertDataSchema(e.event))
      .setEventHandler[DataSchemaUpdated](e => updateDataSchema(e.event))
      .setEventHandler[DataSchemaDeleted](e => deleteDataSchema(e.event))
      .build
  }

  def aggregateTags = DataSchemaEvent.Tag.allTags

  private def createTables() = {
    for {
      _ <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS data_schemas (
          key text PRIMARY KEY,
          name text,
          description text
        )
      """)

    } yield Done
  }

  private def prepareStatements() =
    for {
      insertDataSchema <- session.prepare("INSERT INTO data_schemas(key, name, description) VALUES (:key, :name, :description)")
      updateDataSchema <- session.prepare("UPDATE data_schemas SET name = :name, description = :description WHERE key = :key")
      deleteDataSchema <- session.prepare("DELETE FROM data_schemas WHERE key = :key")
    } yield {
      insertDataSchemaStatement = insertDataSchema
      updateDataSchemaStatement = updateDataSchema
      deleteDataSchemaStatement = deleteDataSchema
      Done
    }

  private def insertDataSchema(event: DataSchemaCreated) = {
    Future.successful(
      List(
        insertDataSchemaStatement
          .bind()
          .setString("key", event.dataSchema.key)
          .setString("name", event.dataSchema.name)
          .setString("description", event.dataSchema.description.getOrElse(""))
      ))
  }

  private def updateDataSchema(event: DataSchemaUpdated) = {
    Future.successful(
      List(
        updateDataSchemaStatement
          .bind()
          .setString("key", event.dataSchema.key)
          .setString("name", event.dataSchema.name)
          .setString("description", event.dataSchema.description.getOrElse(""))
      ))
  }

  private def deleteDataSchema(event: DataSchemaDeleted) = {
    Future.successful(
      List(
        deleteDataSchemaStatement.bind().setString("key", event.key)
      ))
  }

}
