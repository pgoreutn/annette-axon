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
import org.slf4j.{Logger, LoggerFactory}
import com.typesafe.config.Config
import org.camunda.bpm.engine.{ProcessEngine, ProcessEngineConfiguration}
import org.camunda.bpm.engine.impl.cfg.{ProcessEnginePlugin, StandaloneProcessEngineConfiguration}
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin
import play.api.inject.Injector

import scala.util.Try

object ProcessEngineProvider {

  final private val log: Logger = LoggerFactory.getLogger(this.getClass)

  def create(config: Config, injector: Injector, system: ActorSystem): ProcessEngine = {

    val dbConf = config.getConfig(s"processEngine.db")
    val driver = dbConf.getString("driver")
    val url = dbConf.getString("url")
    println(s"Camunda DB url: $url")
    val username = dbConf.getString("username")
    val password = dbConf.getString("password")
    val maxActiveConnections = Try { dbConf.getInt("max-active-connections") }.getOrElse(20)

    log.debug("ProcessEngine starting...")
    val plugins = new java.util.ArrayList[ProcessEnginePlugin]()
    plugins.add(new SpinProcessEnginePlugin().asInstanceOf[ProcessEnginePlugin])
    val peConf = new StandaloneProcessEngineConfiguration()
    peConf.setProcessEnginePlugins(plugins)

    val processEngine = peConf
      .setClassLoader(ClassLoader.getSystemClassLoader.getParent)
      .setHistory(ProcessEngineConfiguration.HISTORY_FULL)
      .setJdbcDriver(driver)
      .setJdbcUrl(url)
      .setJdbcUsername(username)
      .setJdbcPassword(password)
      .setJdbcMaxActiveConnections(maxActiveConnections)
      .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE)
      .setJobExecutorActivate(true)
      //.setExpressionManager(new CustomExpressionManager)
      .buildProcessEngine()
    log.debug("ProcessEngine {} started", processEngine.getName)
    processEngine
  }

}
