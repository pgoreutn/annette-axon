package axon.rest.bpm.permission

import annette.authorization.api.Permission

object BpmPermissions {

  final val BPM_REPOSITORY_CONTROL = Permission("axon.bpm.repository.control")
  final val BPM_DIAGRAM_VIEW = Permission("axon.bpm.repository.bpmDiagram.view")
  final val PROCESS_DEF_VIEW = Permission("axon.bpm.deployment.view")
  final val BUSINESS_PROCESS_VIEW = Permission("axon.bpm.repository.bpmDiagram.view")


}
