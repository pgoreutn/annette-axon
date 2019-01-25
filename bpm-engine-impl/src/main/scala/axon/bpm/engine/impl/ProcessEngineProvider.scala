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
