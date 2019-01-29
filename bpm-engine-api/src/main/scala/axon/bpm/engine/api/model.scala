package axon.bpm.engine.api
import org.camunda.bpm.engine.repository.{CaseDefinition, DecisionDefinition, DecisionRequirementsDefinition, ProcessDefinition}
import play.api.libs.json.{Format, Json}

import scala.collection.JavaConverters._

case class FindProcessDefOptions(key: Option[String] = None, name: Option[String] = None, latest: Boolean = true)

object FindProcessDefOptions {
  implicit val format: Format[FindProcessDefOptions] = Json.format
}

case class FindCaseDefOptions(key: Option[String] = None, name: Option[String] = None, latest: Boolean = true)

object FindCaseDefOptions {
  implicit val format: Format[FindCaseDefOptions] = Json.format
}

case class FindDecisionDefOptions(key: Option[String] = None, name: Option[String] = None, latest: Boolean = true)

object FindDecisionDefOptions {
  implicit val format: Format[FindDecisionDefOptions] = Json.format
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

case class DecisionDef(
    id: String,
    key: String,
    name: String,
    version: Int,
    versionTag: String,
    decisionRequirementsDefinitionId: String,
    decisionRequirementsDefinitionKey: String,
)

object DecisionDef {
  def apply(pd: DecisionDefinition): DecisionDef =
    DecisionDef(
      id = pd.getId,
      key = pd.getKey,
      name = Option(pd.getName).getOrElse(""),
      // category = Option(pd.getCategory).getOrElse(""),
      version = pd.getVersion,
      versionTag = Option(pd.getVersionTag).getOrElse(""),
      decisionRequirementsDefinitionId = Option(pd.getDecisionRequirementsDefinitionId).getOrElse(""),
      decisionRequirementsDefinitionKey = Option(pd.getDecisionRequirementsDefinitionKey).getOrElse(""),
    )

  implicit val format: Format[DecisionDef] = Json.format
}

case class DecisionRequirementsDef(
    id: String,
    key: String,
    name: String,
    version: Int,
)

object DecisionRequirementsDef {
  def apply(pd: DecisionRequirementsDefinition): DecisionRequirementsDef =
    DecisionRequirementsDef(
      id = pd.getId,
      key = pd.getKey,
      name = Option(pd.getName).getOrElse(""),
      // category = Option(pd.getCategory).getOrElse(""),
      version = pd.getVersion,
    )

  implicit val format: Format[DecisionRequirementsDef] = Json.format
}

case class CaseDef(
    id: String,
    key: String,
    name: String,
    version: Int,
)

object CaseDef {
  def apply(cs: CaseDefinition): CaseDef =
    CaseDef(
      id = cs.getId,
      key = cs.getKey,
      name = Option(cs.getName).getOrElse(""),
      version = cs.getVersion,
    )

  implicit val format: Format[CaseDef] = Json.format
}

case class DeploymentWithDefs(
    id: String,
    name: String,
    source: String,
    processDefs: Seq[ProcessDef],
    caseDefs: Seq[CaseDef],
    decisionDefs: Seq[DecisionDef],
    decisionRequirementsDefs: Seq[DecisionRequirementsDef],
)

object DeploymentWithDefs {
  def apply(depl: org.camunda.bpm.engine.repository.DeploymentWithDefinitions): DeploymentWithDefs = {
    val processDefs = Option(depl.getDeployedProcessDefinitions)
      .getOrElse(new java.util.ArrayList[ProcessDefinition]())
      .asScala
      .map(ProcessDef.apply)
    val caseDefs = Option(depl.getDeployedCaseDefinitions)
      .getOrElse(new java.util.ArrayList[CaseDefinition]())
      .asScala
      .map(CaseDef.apply)
    val decisionDefs = Option(depl.getDeployedDecisionDefinitions)
      .getOrElse(new java.util.ArrayList[DecisionDefinition]())
      .asScala
      .map(DecisionDef.apply)
    val decisionRequirementsDefs = Option(depl.getDeployedDecisionRequirementsDefinitions)
      .getOrElse(new java.util.ArrayList[DecisionRequirementsDefinition]())
      .asScala
      .map(DecisionRequirementsDef.apply)

    DeploymentWithDefs(
      id = depl.getId,
      name = Option(depl.getName).getOrElse(""),
      source = Option(depl.getSource).getOrElse(""),
      processDefs = processDefs,
      caseDefs = caseDefs,
      decisionDefs = decisionDefs,
      decisionRequirementsDefs = decisionRequirementsDefs,
    )
  }

  implicit val format: Format[DeploymentWithDefs] = Json.format
}
