package axon.rest.bpm
import annette.authorization.api.Permission

object BpmPermissions {
  val CREATE_SCHEMA = Permission("axon.bpm.schema.create")
  val UPDATE_SCHEMA = Permission("axon.bpm.schema.update")
  val DELETE_SCHEMA = Permission("axon.bpm.schema.delete")
  final val VIEW_SCHEMA = Permission("axon.bpm.schema.view")
}
