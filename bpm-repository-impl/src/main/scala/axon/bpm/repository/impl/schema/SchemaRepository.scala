package axon.bpm.repository.impl.schema

import axon.bpm.repository.api.SchemaSummary
import com.datastax.driver.core.Row
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

private[impl] class SchemaRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  def findSchemas(filter: String): Future[immutable.Seq[SchemaSummary]] = {
    val filterLC = filter.toLowerCase
    // Don't use in production
    for {
      seq <- selectSchemas
    } yield {
      if (filter.isEmpty) {
        seq.to[collection.immutable.Seq]
      } else {

        seq
          .filter { summary =>
              summary.name.toLowerCase.contains(filterLC) ||
              summary.notation.toLowerCase.contains(filterLC) ||
              summary.description.getOrElse("").toLowerCase.contains(filterLC) ||
              summary.processDefinitions.getOrElse("").toLowerCase.contains(filterLC)
          }
          .to[collection.immutable.Seq]
      }
    }
  }

  private def selectSchemas = {
    session.selectAll("SELECT * FROM schemas").map(_.map(convertSchemaSummary))
  }

  private def convertSchemaSummary(schema: Row): SchemaSummary = {
    SchemaSummary(
      schema.getString("id"),
      schema.getString("name"), {
        val s = schema.getString("description")
        if (s.nonEmpty) Some(s) else None
      },
      schema.getString("notation"),
      {
        val s = schema.getString("process_definitions")
        if (s.nonEmpty) Some(s) else None
      }
    )
  }
}
