package axon.knowledge.repository.api
import axon.knowledge.repository.api.builder.DataStructDefFinder
import axon.knowledge.repository.api.model.{DataSchema, DataSchemaKey}

import scala.concurrent.{ExecutionContext, Future}

case class DataStructRepo(repo: Map[DataSchemaKey, DataSchema] = Map.empty) extends DataStructDefFinder {

  def add(ds: DataSchema) = this.copy(repo + (ds.key -> ds))

  override def find(key: DataSchemaKey)(implicit ec: ExecutionContext): Future[DataSchema] = {
    Future {
      //throw new Exception("Find exception")
      val res = repo(key)
      if (key == "") {
        println(res)
      }
      res
    }
  }
}
