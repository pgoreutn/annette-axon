package annette.authorization.impl

import annette.authorization.api.RoleSummary
import com.datastax.driver.core.Row
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

private[impl] class RoleRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  def findRoles(filter: String): Future[immutable.Set[RoleSummary]] = {
    val filterLC = filter.toLowerCase
    // Don't use in production
    for {
      seq <- selectRoles
    } yield {
      if (filter.isEmpty) {
        seq.to[collection.immutable.Set]
      } else {

        seq
          .filter { summary =>
            summary.id.toLowerCase.contains(filterLC) ||
              summary.name.toLowerCase.contains(filterLC) ||
              summary.description.getOrElse("").toLowerCase.contains(filterLC)
          }
          .to[collection.immutable.Set]
      }
    }
  }

  private def selectRoles = {
    session.selectAll("SELECT * FROM roles").map(_.map(convertRoleSummary))
  }

  private def convertRoleSummary(role: Row): RoleSummary = {
    RoleSummary(
      role.getString("id"),
      role.getString("name"), {
        val s = role.getString("description")
        if (s.nonEmpty) Some(s) else None
      }
    )
  }
}
