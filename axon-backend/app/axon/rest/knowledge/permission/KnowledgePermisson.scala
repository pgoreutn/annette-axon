package axon.rest.knowledge.permission
import annette.authorization.api.Permission

object KnowledgePermissions {
  val CREATE_DATA_SCHEMA = Permission("axon.knowledge.repository.dataSchema.create")
  val UPDATE_DATA_SCHEMA = Permission("axon.knowledge.repository.dataSchema.update")
  val DELETE_DATA_SCHEMA = Permission("axon.knowledge.repository.dataSchema.delete")
  final val VIEW_DATA_SCHEMA = Permission("axon.knowledge.repository.dataSchema.view")
}

