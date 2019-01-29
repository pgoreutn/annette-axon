package axon.knowledge.repository.api
import axon.knowledge.repository.api.builder.DataStructDefFinder
import axon.knowledge.repository.api.model.{DataStructDef, DataStructKey}

import scala.concurrent.Future

case class DataStructRepo(repo: Map[DataStructKey, DataStructDef] = Map.empty) extends DataStructDefFinder {

  def add(ds: DataStructDef) = this.copy(repo + (ds.key -> ds))

  override def find(key: DataStructKey): Future[DataStructDef] = {
    Future.successful(repo(key))
  }
}
