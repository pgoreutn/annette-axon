package axon.rest.bpm
import annette.authorization.api.Permission

object BpmPermissions {
  val CREATE_BPM_DIAGRAM = Permission("axon.bpm.repository.bpmDiagram.create")
  val UPDATE_BPM_DIAGRAM = Permission("axon.bpm.repository.bpmDiagram.update")
  val DELETE_BPM_DIAGRAM = Permission("axon.bpm.repository.bpmDiagram.delete")
  final val VIEW_BPM_DIAGRAM = Permission("axon.bpm.repository.bpmDiagram.view")
}
