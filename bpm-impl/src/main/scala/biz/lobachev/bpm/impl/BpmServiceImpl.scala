package biz.lobachev.bpm.impl

import biz.lobachev.bpm.api.BpmService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.Future

/**
  * Implementation of the BpmService.
  */
class BpmServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends BpmService {

  override def hello(id: String) = ServiceCall { _ =>
    Future.successful(s"Hello, $id")
  }


}
