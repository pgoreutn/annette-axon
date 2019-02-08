package axon.bpm.repository.impl.diagram

import axon.bpm.repository.api.model.BpmDiagramSummary
import com.datastax.driver.core.Row
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

private[impl] class BpmDiagramRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  def findBpmDiagrams(filter: String): Future[immutable.Seq[BpmDiagramSummary]] = {
    val filterLC = filter.toLowerCase
    // Don't use in production
    for {
      seq <- selectBpmDiagrams
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

  private def selectBpmDiagrams = {
    session.selectAll("SELECT * FROM bpmDiagrams").map(_.map(convertBpmDiagramSummary))
  }

  private def convertBpmDiagramSummary(bpmDiagram: Row): BpmDiagramSummary = {
    BpmDiagramSummary(
      bpmDiagram.getString("id"),
      bpmDiagram.getString("name"), {
        val s = bpmDiagram.getString("description")
        if (s.nonEmpty) Some(s) else None
      },
      bpmDiagram.getString("notation"), {
        val s = bpmDiagram.getString("process_definitions")
        if (s.nonEmpty) Some(s) else None
      }
    )
  }
}
