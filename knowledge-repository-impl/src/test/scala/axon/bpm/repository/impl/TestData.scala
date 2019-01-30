package axon.knowledge.repository.impl

import scala.xml.XML

object TestData {
  def xmlSchema(id: String = "id", name: String = "name", description: String = "description") = s"""|<?xml version="1.0" encoding="UTF-8"?>
                     |<knowledgen:definitions xmlns:knowledgen="http://www.omg.org/spec/KNOWLEDGEN/20100524/MODEL"
                     |                  xmlns:knowledgendi="http://www.omg.org/spec/KNOWLEDGEN/20100524/DI"
                     |                  xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                     |                  xmlns:camunda="http://camunda.org/schema/1.0/knowledgen" id="Definitions_1"
                     |                  targetNamespace="http://knowledgen.io/schema/knowledgen" exporter="Camunda Modeler" exporterVersion="1.11.3">
                     |    <knowledgen:process id="$id" name="$name" isExecutable="true">
                     |        <knowledgen:documentation>$description</knowledgen:documentation>
                     |        <knowledgen:startEvent id="StartEvent_1">
                     |            <knowledgen:outgoing>SequenceFlow_07tsabf</knowledgen:outgoing>
                     |        </knowledgen:startEvent>
                     |        <knowledgen:sequenceFlow id="SequenceFlow_07tsabf" sourceRef="StartEvent_1" targetRef="Task_17fss0p"/>
                     |        <knowledgen:sequenceFlow id="SequenceFlow_117trsf" sourceRef="Task_17fss0p" targetRef="Task_1l638jw"/>
                     |        <knowledgen:sequenceFlow id="SequenceFlow_0voctu5" sourceRef="Task_1l638jw" targetRef="Task_0to6q24"/>
                     |        <knowledgen:userTask id="Task_17fss0p" name="Задача проектного процесса 1" camunda:formKey="projectForm"
                     |                       camunda:assignee="$${initiatorId}">
                     |            <knowledgen:incoming>SequenceFlow_07tsabf</knowledgen:incoming>
                     |            <knowledgen:outgoing>SequenceFlow_117trsf</knowledgen:outgoing>
                     |        </knowledgen:userTask>
                     |        <knowledgen:userTask id="Task_1l638jw" name="Задача проектного процесса 2" camunda:formKey="projectForm2"
                     |                       camunda:assignee="$${initiatorId}">
                     |            <knowledgen:incoming>SequenceFlow_117trsf</knowledgen:incoming>
                     |            <knowledgen:outgoing>SequenceFlow_0voctu5</knowledgen:outgoing>
                     |        </knowledgen:userTask>
                     |        <knowledgen:userTask id="Task_0to6q24" name="Задача проектного процесса 3" camunda:formKey="projectForm3"
                     |                       camunda:assignee="$${initiatorId}">
                     |            <knowledgen:incoming>SequenceFlow_0voctu5</knowledgen:incoming>
                     |            <knowledgen:outgoing>SequenceFlow_0fgwq9a</knowledgen:outgoing>
                     |        </knowledgen:userTask>
                     |        <knowledgen:endEvent id="EndEvent_0cl37qe">
                     |            <knowledgen:incoming>SequenceFlow_0fgwq9a</knowledgen:incoming>
                     |        </knowledgen:endEvent>
                     |        <knowledgen:sequenceFlow id="SequenceFlow_0fgwq9a" sourceRef="Task_0to6q24" targetRef="EndEvent_0cl37qe"/>
                     |    </knowledgen:process>
                     |    <knowledgendi:KNOWLEDGENDiagram id="KNOWLEDGENDiagram_1">
                     |        <knowledgendi:KNOWLEDGENPlane id="KNOWLEDGENPlane_1" knowledgenElement="projectProcess">
                     |            <knowledgendi:KNOWLEDGENShape id="_KNOWLEDGENShape_StartEvent_2" knowledgenElement="StartEvent_1">
                     |                <dc:Bounds x="173" y="102" width="36" height="36"/>
                     |            </knowledgendi:KNOWLEDGENShape>
                     |            <knowledgendi:KNOWLEDGENEdge id="SequenceFlow_07tsabf_di" knowledgenElement="SequenceFlow_07tsabf">
                     |                <di:waypoint x="209" y="120"/>
                     |                <di:waypoint x="259" y="120"/>
                     |                <knowledgendi:KNOWLEDGENLabel>
                     |                    <dc:Bounds x="234" y="98.5" width="0" height="13"/>
                     |                </knowledgendi:KNOWLEDGENLabel>
                     |            </knowledgendi:KNOWLEDGENEdge>
                     |            <knowledgendi:KNOWLEDGENEdge id="SequenceFlow_117trsf_di" knowledgenElement="SequenceFlow_117trsf">
                     |                <di:waypoint x="359" y="120"/>
                     |                <di:waypoint x="409" y="120"/>
                     |                <knowledgendi:KNOWLEDGENLabel>
                     |                    <dc:Bounds x="384" y="98.5" width="0" height="13"/>
                     |                </knowledgendi:KNOWLEDGENLabel>
                     |            </knowledgendi:KNOWLEDGENEdge>
                     |            <knowledgendi:KNOWLEDGENEdge id="SequenceFlow_0voctu5_di" knowledgenElement="SequenceFlow_0voctu5">
                     |                <di:waypoint x="509" y="120"/>
                     |                <di:waypoint x="559" y="120"/>
                     |                <knowledgendi:KNOWLEDGENLabel>
                     |                    <dc:Bounds x="534" y="98.5" width="0" height="13"/>
                     |                </knowledgendi:KNOWLEDGENLabel>
                     |            </knowledgendi:KNOWLEDGENEdge>
                     |            <knowledgendi:KNOWLEDGENShape id="UserTask_0p0eaqo_di" knowledgenElement="Task_17fss0p">
                     |                <dc:Bounds x="259" y="80" width="100" height="80"/>
                     |            </knowledgendi:KNOWLEDGENShape>
                     |            <knowledgendi:KNOWLEDGENShape id="UserTask_02djmjg_di" knowledgenElement="Task_1l638jw">
                     |                <dc:Bounds x="409" y="80" width="100" height="80"/>
                     |            </knowledgendi:KNOWLEDGENShape>
                     |            <knowledgendi:KNOWLEDGENShape id="UserTask_1me6zcg_di" knowledgenElement="Task_0to6q24">
                     |                <dc:Bounds x="559" y="80" width="100" height="80"/>
                     |            </knowledgendi:KNOWLEDGENShape>
                     |            <knowledgendi:KNOWLEDGENShape id="EndEvent_0cl37qe_di" knowledgenElement="EndEvent_0cl37qe">
                     |                <dc:Bounds x="709" y="102" width="36" height="36"/>
                     |                <knowledgendi:KNOWLEDGENLabel>
                     |                    <dc:Bounds x="727" y="141" width="0" height="13"/>
                     |                </knowledgendi:KNOWLEDGENLabel>
                     |            </knowledgendi:KNOWLEDGENShape>
                     |            <knowledgendi:KNOWLEDGENEdge id="SequenceFlow_0fgwq9a_di" knowledgenElement="SequenceFlow_0fgwq9a">
                     |                <di:waypoint x="659" y="120"/>
                     |                <di:waypoint x="709" y="120"/>
                     |                <knowledgendi:KNOWLEDGENLabel>
                     |                    <dc:Bounds x="684" y="98" width="0" height="13"/>
                     |                </knowledgendi:KNOWLEDGENLabel>
                     |            </knowledgendi:KNOWLEDGENEdge>
                     |        </knowledgendi:KNOWLEDGENPlane>
                     |    </knowledgendi:KNOWLEDGENDiagram>
                     |</knowledgen:definitions>
                  """.stripMargin

  val xml = XML.loadString(xmlSchema())
  val id = (xml \\ "process" \ "@id").text
  val name = (xml \\ "process" \ "@name").text
  val description = (xml \\ "process" \ "documentation").text


}
