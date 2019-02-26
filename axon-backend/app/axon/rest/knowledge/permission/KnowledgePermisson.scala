package axon.rest.knowledge.permission
import annette.authorization.api.Permission

object KnowledgePermissions {

  final val KNOWLEDGE_REPOSITORY_CONTROL = Permission("axon.knowledge.repository.control")
  final val DATA_SCHEMA_VIEW = Permission("axon.knowledge.repository.dataSchema.view")
}

