package axon.knowledge.repository.api
import axon.knowledge.repository.api.builder.DataStructDefFinder
import axon.knowledge.repository.api.model.{DataSchema, DataSchemaKey}

import scala.concurrent.Future

case class DataStructRepo(repo: Map[DataSchemaKey, DataSchema] = Map.empty) extends DataStructDefFinder {

  def add(ds: DataSchema) = this.copy(repo + (ds.key -> ds))

  override def find(key: DataSchemaKey): Future[DataSchema] = {
    Future.successful(repo(key))
  }
}
