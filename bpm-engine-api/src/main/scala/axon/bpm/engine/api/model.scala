package axon.bpm.engine.api
import org.camunda.bpm.engine.repository.ProcessDefinition
import play.api.libs.json.{Format, Json}
import scala.collection.JavaConverters._

case class FindProcessDefOptions(key: Option[String] = None, name: Option[String] = None, latest: Boolean = true)

object FindProcessDefOptions {
  implicit val format: Format[FindProcessDefOptions] = Json.format
}

case class ProcessDef(
    id: String,
    key: String,
    name: String,
    description: String,
    // category: String,
    hasStartFormKey: Boolean,
    version: Int,
    versionTag: String,
    isSuspended: Boolean,
)

object ProcessDef {
  def apply(pd: ProcessDefinition): ProcessDef =
    ProcessDef(
      id = pd.getId,
      key = pd.getKey,
      name = Option(pd.getName).getOrElse(""),
      description = Option(pd.getDescription).getOrElse(""),
      // category = Option(pd.getCategory).getOrElse(""),
      hasStartFormKey = pd.hasStartFormKey,
      version = pd.getVersion,
      versionTag = Option(pd.getVersionTag).getOrElse(""),
      isSuspended = pd.isSuspended,
    )

  implicit val format: Format[ProcessDef] = Json.format
}

case class DeploymentWithDefs(
    id: String,
    name: String,
    source: String,
    processDefs: Seq[ProcessDef]
)

object DeploymentWithDefs {
  def apply(depl: org.camunda.bpm.engine.repository.DeploymentWithDefinitions): DeploymentWithDefs = {
    val processDefs = Option(depl.getDeployedProcessDefinitions)
      .getOrElse(new java.util.ArrayList[ProcessDefinition]())
      .asScala
      .map(ProcessDef.apply)

    DeploymentWithDefs(
      id = depl.getId,
      name = Option(depl.getName).getOrElse(""),
      source = Option(depl.getSource).getOrElse(""),
      processDefs = processDefs
    )
  }

  implicit val format: Format[DeploymentWithDefs] = Json.format
}
