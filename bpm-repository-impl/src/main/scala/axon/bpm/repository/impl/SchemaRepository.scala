package axon.bpm.repository.impl
import axon.bpm.repository.api.SchemaSummary
import com.datastax.driver.core.Row
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

private[impl] class SchemaRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  def findSchemas(filter: String): Future[immutable.Seq[SchemaSummary]] = {
    // Don't use in production
    for {
      seq <- selectSchemas
    } yield {
      if (filter.isEmpty) {
        seq.to[collection.immutable.Seq]
      } else {

        seq
          .filter { summary =>
            summary.id.contains(filter) || summary.name.contains(filter) ||
              summary.description.getOrElse("").contains(filter)
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
      schema.getString("notation")
    )
  }
}
