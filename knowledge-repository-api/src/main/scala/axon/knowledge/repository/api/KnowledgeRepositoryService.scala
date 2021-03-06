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

package axon.knowledge.repository.api

import akka.{Done, NotUsed}
import annette.shared.exceptions.AnnetteExceptionSerializer
import axon.knowledge.repository.api.model.{DataSchema, DataSchemaKey, DataSchemaSummary}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.Environment

import scala.collection._

trait KnowledgeRepositoryService extends Service {

  def createDataSchema: ServiceCall[DataSchema, DataSchema]
  def updateDataSchema: ServiceCall[DataSchema, DataSchema]
  def deleteDataSchema(key: DataSchemaKey): ServiceCall[NotUsed, Done]
  def buildSingleLevel(key: String): ServiceCall[NotUsed, DataSchema]
  def buildMultiLevel(key: String): ServiceCall[NotUsed, DataSchema]
  def findDataSchemaByKey(key: String): ServiceCall[NotUsed, DataSchema]
  def findDataSchema: ServiceCall[String, immutable.Seq[DataSchemaSummary]]
  def findDataSchemaByKeys: ServiceCall[immutable.Seq[DataSchemaKey], immutable.Seq[DataSchemaSummary]]

  final override def descriptor = {
    import Service._
    // @formatter:off
    named("knowledge-repository")
      .withCalls(
        restCall(Method.POST, "/api/knowledge/repository/dataSchema", createDataSchema),
        restCall(Method.PUT, "/api/knowledge/repository/dataSchema", updateDataSchema),
        restCall(Method.DELETE, "/api/knowledge/repository/dataSchema/:key", deleteDataSchema _),
        restCall(Method.GET, "/api/knowledge/repository/dataSchema/:key", findDataSchemaByKey _),
        restCall(Method.GET, "/api/knowledge/repository/buildSingleLevel/:key", buildSingleLevel _),
        restCall(Method.GET, "/api/knowledge/repository/buildMultiLevel/:key", buildMultiLevel _),
        restCall(Method.POST, "/api/knowledge/repository/findDataSchema", findDataSchema _),
        restCall(Method.POST, "/api/knowledge/repository/findDataSchemaByKeys", findDataSchemaByKeys _),
      )
      .withExceptionSerializer(new AnnetteExceptionSerializer())
      .withAutoAcl(true)
    // @formatter:on
  }
}
