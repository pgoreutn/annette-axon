package axon.knowledge.repository.impl.schema

import axon.knowledge.repository.api.model.{DataSchemaKey, DataSchemaSummary}
import com.datastax.driver.core.Row
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConverters._

private[impl] class DataSchemaRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  def findDataSchemaByKeys(keys: immutable.Seq[DataSchemaKey]): Future[immutable.Seq[DataSchemaSummary]] = {
    for {
      stmt <- session.prepare("SELECT * FROM data_schemas WHERE key IN ?")
      result <- session.selectAll(stmt.bind(keys.toList.asJava)).map(_.map(convertDataSchemaSummary))
    } yield result.to[collection.immutable.Seq]
  }

  def findDataSchemas(filter: String): Future[immutable.Seq[DataSchemaSummary]] = {
    val filterLC = filter.toLowerCase
    // Don't use in production
    for {
      seq <- selectDataSchemas
    } yield {
      if (filter.isEmpty) {
        seq.to[collection.immutable.Seq]
      } else {

        seq
          .filter { summary =>
            summary.key.toLowerCase.contains(filterLC) ||
            summary.name.toLowerCase.contains(filterLC) ||
            summary.description.getOrElse("").toLowerCase.contains(filterLC)
          }
          .to[collection.immutable.Seq]
      }
    }
  }

  private def selectDataSchemas = {
    session.selectAll("SELECT * FROM data_schemas").map(_.map(convertDataSchemaSummary))
  }

  private def convertDataSchemaSummary(dataSchema: Row): DataSchemaSummary = {
    DataSchemaSummary(
      dataSchema.getString("key"),
      dataSchema.getString("name"), {
        val s = dataSchema.getString("description")
        if (s.nonEmpty) Some(s) else None
      },
    )
  }
}
