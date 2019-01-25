package axon.bpm.engine.impl

import akka.actor.ActorSystem
import akka.stream.Materializer
import axon.bpm.engine.api._
import axon.bpm.repository.api.Schema
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import org.camunda.bpm.engine.ProcessEngine

import scala.collection.immutable
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Implementation of the BpmService.
  */
class BpmEngineServiceImpl(registry: PersistentEntityRegistry, system: ActorSystem, processEngine: ProcessEngine)(
    implicit ec: ExecutionContext,
    mat: Materializer)
    extends BpmEngineService {

  val repositoryService = processEngine.getRepositoryService

  override def findProcessDef: ServiceCall[FindProcessDefOptions, immutable.Seq[ProcessDef]] = ServiceCall { findOptions =>
    Future.successful {
      var q = repositoryService.createProcessDefinitionQuery()

      q = findOptions.key.filter(_.trim.length != 0).map(k => q.processDefinitionKeyLike(k.trim)).getOrElse(q)
      q = findOptions.name.filter(_.trim.length != 0).map(n => q.processDefinitionNameLike(n.trim)).getOrElse(q)
      q = if (findOptions.latest) { q.latestVersion() } else { q }

      val list = q
        .withoutTenantId()
        .orderByProcessDefinitionName()
        .asc()
        .list()
        .asScala
        .map(pd => ProcessDef.apply(pd))
      immutable.Seq(list: _*)
    }
  }
  override def deploy: ServiceCall[Schema, DeploymentWithDefs] = ServiceCall { schema =>
    Future.successful {
      val result = repositoryService
        .createDeployment()
        .name(schema.name)
        .source(schema.id)
        .addString(s"${schema.name}.${schema.notation.toLowerCase}", schema.xml)
        .deployWithResult()
      DeploymentWithDefs(result)
    }
  }
}
