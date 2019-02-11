package axon.bpm.repository.impl.process
import axon.bpm.repository.api.model.{BusinessProcessSummary, ProcessReferenceById, ProcessReferenceByKey}
import com.datastax.driver.core.Row
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

private[impl] class BusinessProcessRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  def findBusinessProcess(filter: String): Future[immutable.Seq[BusinessProcessSummary]] = {
    val filterLC = filter.toLowerCase
    // Don't use in production
    for {
      seq <- selectBusinessProcesses
    } yield {
      if (filter.isEmpty) {
        seq.to[collection.immutable.Seq]
      } else {
        seq
          .filter { summary =>
            val processReference = summary.processReference match {
              case ProcessReferenceByKey(key) => key
              case ProcessReferenceById(id)   => ""
            }
            summary.name.toLowerCase.contains(filterLC) ||
            summary.description.getOrElse("").toLowerCase.contains(filterLC) ||
            processReference.toLowerCase.contains(filterLC) ||
            summary.dataSchemaKey.toLowerCase.contains(filterLC)
          }
          .to[collection.immutable.Seq]
      }
    }
  }

  private def selectBusinessProcesses = {
    session.selectAll("SELECT * FROM business_processes").map(_.map(convertBusinessProcessSummary))
  }

  private def convertBusinessProcessSummary(businessProcess: Row): BusinessProcessSummary = {
    val desc = businessProcess.getString("description")
    val description = if (desc.nonEmpty) Some(desc) else None

    val refType = businessProcess.getString("ref_type")
    val procRef = businessProcess.getString("process_reference")
    val processReference =
      if (refType == "id") ProcessReferenceById(procRef)
      else ProcessReferenceByKey(procRef)

    BusinessProcessSummary(
      id = businessProcess.getString("id"),
      name = businessProcess.getString("name"),
      description = description,
      processReference = processReference,
      dataSchemaKey = businessProcess.getString("data_schema_key")
    )
  }
}
