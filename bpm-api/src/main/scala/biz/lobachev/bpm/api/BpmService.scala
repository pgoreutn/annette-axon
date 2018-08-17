package biz.lobachev.bpm.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

/**
  * The bpm service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the BpmService.
  */
trait BpmService extends Service {

  /**
    * Example: curl http://localhost:9000/api/hello/Alice
    */
  def hello(id: String): ServiceCall[NotUsed, String]



  override final def descriptor = {
    import Service._
    // @formatter:off
    named("bpm")
      .withCalls(
        pathCall("/api/hello/:id", hello _)
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}
