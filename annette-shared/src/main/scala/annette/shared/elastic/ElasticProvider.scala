package annette.shared.elastic

import play.api.Configuration
import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties}

object ElasticProvider {

  def create(configuration: Configuration): ElasticClient = {
    val elacticUrl = configuration.getOptional[String]("elastic.url").getOrElse("http://localhost:9200")
    ElasticClient(ElasticProperties(elacticUrl))

  }
}
