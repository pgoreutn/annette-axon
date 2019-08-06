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
