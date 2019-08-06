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

package axon.bpm.engine.impl

import akka.actor.ActorSystem
import akka.stream.Materializer
import axon.bpm.engine.api._
import axon.bpm.repository.api.model.BpmDiagram
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import org.camunda.bpm.engine.ProcessEngine
import play.api.libs.json.{JsObject, Json}

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

  override def findCaseDef: ServiceCall[FindCaseDefOptions, immutable.Seq[CaseDef]] = ServiceCall { findOptions =>
    Future.successful {
      var q = repositoryService.createCaseDefinitionQuery()

      q = findOptions.key.filter(_.trim.length != 0).map(k => q.caseDefinitionKeyLike(k.trim)).getOrElse(q)
      q = findOptions.name.filter(_.trim.length != 0).map(n => q.caseDefinitionNameLike(n.trim)).getOrElse(q)
      q = if (findOptions.latest) { q.latestVersion() } else { q }

      val list = q
        .withoutTenantId()
        .orderByCaseDefinitionName()
        .asc()
        .list()
        .asScala
        .map(pd => CaseDef.apply(pd))
      immutable.Seq(list: _*)
    }
  }

  override def findDecisionDef: ServiceCall[FindDecisionDefOptions, immutable.Seq[DecisionDef]] = ServiceCall { findOptions =>
    Future.successful {
      var q = repositoryService.createDecisionDefinitionQuery()

      q = findOptions.key.filter(_.trim.length != 0).map(k => q.decisionDefinitionKeyLike(k.trim)).getOrElse(q)
      q = findOptions.name.filter(_.trim.length != 0).map(n => q.decisionDefinitionNameLike(n.trim)).getOrElse(q)
      q = if (findOptions.latest) { q.latestVersion() } else { q }

      val list = q
        .withoutTenantId()
        .orderByDecisionDefinitionName()
        .asc()
        .list()
        .asScala
        .map(pd => DecisionDef.apply(pd))
      immutable.Seq(list: _*)
    }
  }

  override def deploy: ServiceCall[BpmDiagram, DeploymentWithDefs] = ServiceCall { bpmDiagram =>
    Future.successful {
      val result = repositoryService
        .createDeployment()
        .name(bpmDiagram.name)
        .source(bpmDiagram.id)
        .addString(s"${bpmDiagram.name}.${bpmDiagram.notation.toLowerCase}", bpmDiagram.xml)
        .deployWithResult()
      DeploymentWithDefs(result)
    }
  }

  override def findProcessDefByIds: ServiceCall[immutable.Seq[String], immutable.Seq[ProcessDef]] = ServiceCall { ids =>
    Future.successful {
      var list = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionIdIn(ids.filter(_.trim.nonEmpty): _*)
        .withoutTenantId()
        .orderByProcessDefinitionName()
        .asc()
        .list()
        .asScala
        .map(pd => ProcessDef.apply(pd))
      immutable.Seq(list: _*)
    }
  }

  override def findProcessDefByKeys: ServiceCall[immutable.Seq[String], immutable.Seq[ProcessDef]] = ServiceCall { keys =>
    Future.successful {
      var list = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKeysIn(keys.filter(_.trim.nonEmpty): _*)
        .latestVersion()
        .withoutTenantId()
        .orderByProcessDefinitionName()
        .asc()
        .list()
        .asScala
        .map(pd => ProcessDef.apply(pd))
      immutable.Seq(list: _*)
    }
  }
}
