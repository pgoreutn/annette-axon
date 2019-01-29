package axon.bpm.engine.impl

import scala.xml.XML

object TestData {

  def xmlSchema(id: String = "id", name: String = "name", description: String = "description") = s"""|<?xml version="1.0" encoding="UTF-8"?>
                     |<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                     |                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                     |                  xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                     |                  xmlns:camunda="http://camunda.org/schema/1.0/bpmn" id="Definitions_1"
                     |                  targetNamespace="http://bpmn.io/schema/bpmn" exporter="Camunda Modeler" exporterVersion="1.11.3">
                     |    <bpmn:process id="$id" name="$name" isExecutable="true">
                     |        <bpmn:documentation>$description</bpmn:documentation>
                     |        <bpmn:startEvent id="StartEvent_1">
                     |            <bpmn:outgoing>SequenceFlow_07tsabf</bpmn:outgoing>
                     |        </bpmn:startEvent>
                     |        <bpmn:sequenceFlow id="SequenceFlow_07tsabf" sourceRef="StartEvent_1" targetRef="Task_17fss0p"/>
                     |        <bpmn:sequenceFlow id="SequenceFlow_117trsf" sourceRef="Task_17fss0p" targetRef="Task_1l638jw"/>
                     |        <bpmn:sequenceFlow id="SequenceFlow_0voctu5" sourceRef="Task_1l638jw" targetRef="Task_0to6q24"/>
                     |        <bpmn:userTask id="Task_17fss0p" name="Задача проектного процесса 1" camunda:formKey="projectForm"
                     |                       camunda:assignee="$${initiatorId}">
                     |            <bpmn:incoming>SequenceFlow_07tsabf</bpmn:incoming>
                     |            <bpmn:outgoing>SequenceFlow_117trsf</bpmn:outgoing>
                     |        </bpmn:userTask>
                     |        <bpmn:userTask id="Task_1l638jw" name="Задача проектного процесса 2" camunda:formKey="projectForm2"
                     |                       camunda:assignee="$${initiatorId}">
                     |            <bpmn:incoming>SequenceFlow_117trsf</bpmn:incoming>
                     |            <bpmn:outgoing>SequenceFlow_0voctu5</bpmn:outgoing>
                     |        </bpmn:userTask>
                     |        <bpmn:userTask id="Task_0to6q24" name="Задача проектного процесса 3" camunda:formKey="projectForm3"
                     |                       camunda:assignee="$${initiatorId}">
                     |            <bpmn:incoming>SequenceFlow_0voctu5</bpmn:incoming>
                     |            <bpmn:outgoing>SequenceFlow_0fgwq9a</bpmn:outgoing>
                     |        </bpmn:userTask>
                     |        <bpmn:endEvent id="EndEvent_0cl37qe">
                     |            <bpmn:incoming>SequenceFlow_0fgwq9a</bpmn:incoming>
                     |        </bpmn:endEvent>
                     |        <bpmn:sequenceFlow id="SequenceFlow_0fgwq9a" sourceRef="Task_0to6q24" targetRef="EndEvent_0cl37qe"/>
                     |    </bpmn:process>
                     |    <bpmndi:BPMNDiagram id="BPMNDiagram_1">
                     |        <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="projectProcess">
                     |            <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1">
                     |                <dc:Bounds x="173" y="102" width="36" height="36"/>
                     |            </bpmndi:BPMNShape>
                     |            <bpmndi:BPMNEdge id="SequenceFlow_07tsabf_di" bpmnElement="SequenceFlow_07tsabf">
                     |                <di:waypoint x="209" y="120"/>
                     |                <di:waypoint x="259" y="120"/>
                     |                <bpmndi:BPMNLabel>
                     |                    <dc:Bounds x="234" y="98.5" width="0" height="13"/>
                     |                </bpmndi:BPMNLabel>
                     |            </bpmndi:BPMNEdge>
                     |            <bpmndi:BPMNEdge id="SequenceFlow_117trsf_di" bpmnElement="SequenceFlow_117trsf">
                     |                <di:waypoint x="359" y="120"/>
                     |                <di:waypoint x="409" y="120"/>
                     |                <bpmndi:BPMNLabel>
                     |                    <dc:Bounds x="384" y="98.5" width="0" height="13"/>
                     |                </bpmndi:BPMNLabel>
                     |            </bpmndi:BPMNEdge>
                     |            <bpmndi:BPMNEdge id="SequenceFlow_0voctu5_di" bpmnElement="SequenceFlow_0voctu5">
                     |                <di:waypoint x="509" y="120"/>
                     |                <di:waypoint x="559" y="120"/>
                     |                <bpmndi:BPMNLabel>
                     |                    <dc:Bounds x="534" y="98.5" width="0" height="13"/>
                     |                </bpmndi:BPMNLabel>
                     |            </bpmndi:BPMNEdge>
                     |            <bpmndi:BPMNShape id="UserTask_0p0eaqo_di" bpmnElement="Task_17fss0p">
                     |                <dc:Bounds x="259" y="80" width="100" height="80"/>
                     |            </bpmndi:BPMNShape>
                     |            <bpmndi:BPMNShape id="UserTask_02djmjg_di" bpmnElement="Task_1l638jw">
                     |                <dc:Bounds x="409" y="80" width="100" height="80"/>
                     |            </bpmndi:BPMNShape>
                     |            <bpmndi:BPMNShape id="UserTask_1me6zcg_di" bpmnElement="Task_0to6q24">
                     |                <dc:Bounds x="559" y="80" width="100" height="80"/>
                     |            </bpmndi:BPMNShape>
                     |            <bpmndi:BPMNShape id="EndEvent_0cl37qe_di" bpmnElement="EndEvent_0cl37qe">
                     |                <dc:Bounds x="709" y="102" width="36" height="36"/>
                     |                <bpmndi:BPMNLabel>
                     |                    <dc:Bounds x="727" y="141" width="0" height="13"/>
                     |                </bpmndi:BPMNLabel>
                     |            </bpmndi:BPMNShape>
                     |            <bpmndi:BPMNEdge id="SequenceFlow_0fgwq9a_di" bpmnElement="SequenceFlow_0fgwq9a">
                     |                <di:waypoint x="659" y="120"/>
                     |                <di:waypoint x="709" y="120"/>
                     |                <bpmndi:BPMNLabel>
                     |                    <dc:Bounds x="684" y="98" width="0" height="13"/>
                     |                </bpmndi:BPMNLabel>
                     |            </bpmndi:BPMNEdge>
                     |        </bpmndi:BPMNPlane>
                     |    </bpmndi:BPMNDiagram>
                     |</bpmn:definitions>
                  """.stripMargin

  val xml = XML.loadString(xmlSchema())
  val id = (xml \\ "process" \ "@id").text
  val name = (xml \\ "process" \ "@name").text
  val description = (xml \\ "process" \ "documentation").text
  val bpmnSchema =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn" exporter="Camunda Modeler" exporterVersion="1.11.3">
      |  <bpmn:collaboration id="Collaboration_1">
      |    <bpmn:participant id="Participant_03n7rsu" name="bbb" processRef="Process_A" />
      |    <bpmn:participant id="Participant_0d1xsxm" name="aaa" processRef="Process_B" />
      |    <bpmn:messageFlow id="MessageFlow_1ylrvpr" sourceRef="Task_0oqbwhc" targetRef="Task_0tj9s2r" />
      |    <bpmn:messageFlow id="MessageFlow_1s8ab44" sourceRef="Task_0aotcjm" targetRef="Task_1l3v2e6" />
      |  </bpmn:collaboration>
      |  <bpmn:process id="Process_A" name="Process A" isExecutable="true">
      |    <bpmn:laneSet>
      |      <bpmn:lane id="Lane_0wbjq8d">
      |        <bpmn:flowNodeRef>StartEvent_1</bpmn:flowNodeRef>
      |        <bpmn:flowNodeRef>Task_13qc6z9</bpmn:flowNodeRef>
      |        <bpmn:flowNodeRef>EndEvent_116513k</bpmn:flowNodeRef>
      |        <bpmn:flowNodeRef>Task_110o857</bpmn:flowNodeRef>
      |      </bpmn:lane>
      |      <bpmn:lane id="Lane_15hatep">
      |        <bpmn:flowNodeRef>Task_0oqbwhc</bpmn:flowNodeRef>
      |        <bpmn:flowNodeRef>Task_1l3v2e6</bpmn:flowNodeRef>
      |      </bpmn:lane>
      |    </bpmn:laneSet>
      |    <bpmn:startEvent id="StartEvent_1">
      |      <bpmn:outgoing>SequenceFlow_0sqpy2f</bpmn:outgoing>
      |    </bpmn:startEvent>
      |    <bpmn:task id="Task_13qc6z9">
      |      <bpmn:incoming>SequenceFlow_0sqpy2f</bpmn:incoming>
      |      <bpmn:outgoing>SequenceFlow_13wgp3u</bpmn:outgoing>
      |    </bpmn:task>
      |    <bpmn:sequenceFlow id="SequenceFlow_0sqpy2f" sourceRef="StartEvent_1" targetRef="Task_13qc6z9" />
      |    <bpmn:task id="Task_0oqbwhc">
      |      <bpmn:incoming>SequenceFlow_13wgp3u</bpmn:incoming>
      |    </bpmn:task>
      |    <bpmn:sequenceFlow id="SequenceFlow_13wgp3u" sourceRef="Task_13qc6z9" targetRef="Task_0oqbwhc" />
      |    <bpmn:sequenceFlow id="SequenceFlow_1q524kw" sourceRef="Task_1l3v2e6" targetRef="Task_110o857" />
      |    <bpmn:endEvent id="EndEvent_116513k">
      |      <bpmn:incoming>SequenceFlow_1ykmm13</bpmn:incoming>
      |    </bpmn:endEvent>
      |    <bpmn:sequenceFlow id="SequenceFlow_1ykmm13" sourceRef="Task_110o857" targetRef="EndEvent_116513k" />
      |    <bpmn:task id="Task_1l3v2e6">
      |      <bpmn:outgoing>SequenceFlow_1q524kw</bpmn:outgoing>
      |    </bpmn:task>
      |    <bpmn:task id="Task_110o857">
      |      <bpmn:incoming>SequenceFlow_1q524kw</bpmn:incoming>
      |      <bpmn:outgoing>SequenceFlow_1ykmm13</bpmn:outgoing>
      |    </bpmn:task>
      |  </bpmn:process>
      |  <bpmn:process id="Process_B" name="Process B" isExecutable="true">
      |    <bpmn:task id="Task_0tj9s2r">
      |      <bpmn:incoming>SequenceFlow_1ds54ni</bpmn:incoming>
      |      <bpmn:outgoing>SequenceFlow_11x0682</bpmn:outgoing>
      |    </bpmn:task>
      |    <bpmn:task id="Task_0aotcjm">
      |      <bpmn:incoming>SequenceFlow_11x0682</bpmn:incoming>
      |    </bpmn:task>
      |    <bpmn:sequenceFlow id="SequenceFlow_11x0682" sourceRef="Task_0tj9s2r" targetRef="Task_0aotcjm" />
      |    <bpmn:sequenceFlow id="SequenceFlow_1ds54ni" sourceRef="StartEvent_03278kg" targetRef="Task_0tj9s2r" />
      |    <bpmn:startEvent id="StartEvent_03278kg">
      |      <bpmn:outgoing>SequenceFlow_1ds54ni</bpmn:outgoing>
      |    </bpmn:startEvent>
      |  </bpmn:process>
      |  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
      |    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Collaboration_1">
      |      <bpmndi:BPMNShape id="Participant_03n7rsu_di" bpmnElement="Participant_03n7rsu">
      |        <dc:Bounds x="123" y="82" width="600" height="250" />
      |      </bpmndi:BPMNShape>
      |      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1">
      |        <dc:Bounds x="175" y="127" width="36" height="36" />
      |        <bpmndi:BPMNLabel>
      |          <dc:Bounds x="148" y="163" width="90" height="20" />
      |        </bpmndi:BPMNLabel>
      |      </bpmndi:BPMNShape>
      |      <bpmndi:BPMNShape id="Lane_0wbjq8d_di" bpmnElement="Lane_0wbjq8d">
      |        <dc:Bounds x="153" y="82" width="570" height="125" />
      |      </bpmndi:BPMNShape>
      |      <bpmndi:BPMNShape id="Lane_15hatep_di" bpmnElement="Lane_15hatep">
      |        <dc:Bounds x="153" y="207" width="570" height="125" />
      |      </bpmndi:BPMNShape>
      |      <bpmndi:BPMNShape id="Task_13qc6z9_di" bpmnElement="Task_13qc6z9">
      |        <dc:Bounds x="289" y="105" width="100" height="80" />
      |      </bpmndi:BPMNShape>
      |      <bpmndi:BPMNEdge id="SequenceFlow_0sqpy2f_di" bpmnElement="SequenceFlow_0sqpy2f">
      |        <di:waypoint xsi:type="dc:Point" x="211" y="145" />
      |        <di:waypoint xsi:type="dc:Point" x="289" y="145" />
      |        <bpmndi:BPMNLabel>
      |          <dc:Bounds x="250" y="123" width="0" height="13" />
      |        </bpmndi:BPMNLabel>
      |      </bpmndi:BPMNEdge>
      |      <bpmndi:BPMNShape id="Task_0oqbwhc_di" bpmnElement="Task_0oqbwhc">
      |        <dc:Bounds x="289" y="236" width="100" height="80" />
      |      </bpmndi:BPMNShape>
      |      <bpmndi:BPMNEdge id="SequenceFlow_13wgp3u_di" bpmnElement="SequenceFlow_13wgp3u">
      |        <di:waypoint xsi:type="dc:Point" x="339" y="185" />
      |        <di:waypoint xsi:type="dc:Point" x="339" y="236" />
      |        <bpmndi:BPMNLabel>
      |          <dc:Bounds x="354" y="203.5" width="0" height="13" />
      |        </bpmndi:BPMNLabel>
      |      </bpmndi:BPMNEdge>
      |      <bpmndi:BPMNShape id="Participant_0d1xsxm_di" bpmnElement="Participant_0d1xsxm">
      |        <dc:Bounds x="123" y="332" width="600" height="250" />
      |      </bpmndi:BPMNShape>
      |      <bpmndi:BPMNShape id="Task_0tj9s2r_di" bpmnElement="Task_0tj9s2r">
      |        <dc:Bounds x="289" y="388" width="100" height="80" />
      |      </bpmndi:BPMNShape>
      |      <bpmndi:BPMNEdge id="MessageFlow_1ylrvpr_di" bpmnElement="MessageFlow_1ylrvpr">
      |        <di:waypoint xsi:type="dc:Point" x="339" y="316" />
      |        <di:waypoint xsi:type="dc:Point" x="339" y="388" />
      |        <bpmndi:BPMNLabel>
      |          <dc:Bounds x="354" y="345" width="0" height="13" />
      |        </bpmndi:BPMNLabel>
      |      </bpmndi:BPMNEdge>
      |      <bpmndi:BPMNShape id="Task_0aotcjm_di" bpmnElement="Task_0aotcjm">
      |        <dc:Bounds x="456" y="388" width="100" height="80" />
      |      </bpmndi:BPMNShape>
      |      <bpmndi:BPMNEdge id="SequenceFlow_11x0682_di" bpmnElement="SequenceFlow_11x0682">
      |        <di:waypoint xsi:type="dc:Point" x="389" y="428" />
      |        <di:waypoint xsi:type="dc:Point" x="456" y="428" />
      |        <bpmndi:BPMNLabel>
      |          <dc:Bounds x="422.5" y="406" width="0" height="13" />
      |        </bpmndi:BPMNLabel>
      |      </bpmndi:BPMNEdge>
      |      <bpmndi:BPMNShape id="StartEvent_03278kg_di" bpmnElement="StartEvent_03278kg">
      |        <dc:Bounds x="181" y="410" width="36" height="36" />
      |        <bpmndi:BPMNLabel>
      |          <dc:Bounds x="199" y="449" width="0" height="13" />
      |        </bpmndi:BPMNLabel>
      |      </bpmndi:BPMNShape>
      |      <bpmndi:BPMNEdge id="SequenceFlow_1ds54ni_di" bpmnElement="SequenceFlow_1ds54ni">
      |        <di:waypoint xsi:type="dc:Point" x="217" y="428" />
      |        <di:waypoint xsi:type="dc:Point" x="252" y="428" />
      |        <di:waypoint xsi:type="dc:Point" x="252" y="428" />
      |        <di:waypoint xsi:type="dc:Point" x="289" y="428" />
      |        <bpmndi:BPMNLabel>
      |          <dc:Bounds x="267" y="421.5" width="0" height="13" />
      |        </bpmndi:BPMNLabel>
      |      </bpmndi:BPMNEdge>
      |      <bpmndi:BPMNShape id="Task_1l3v2e6_di" bpmnElement="Task_1l3v2e6">
      |        <dc:Bounds x="456" y="236" width="100" height="80" />
      |      </bpmndi:BPMNShape>
      |      <bpmndi:BPMNEdge id="MessageFlow_1s8ab44_di" bpmnElement="MessageFlow_1s8ab44">
      |        <di:waypoint xsi:type="dc:Point" x="506" y="388" />
      |        <di:waypoint xsi:type="dc:Point" x="506" y="352" />
      |        <di:waypoint xsi:type="dc:Point" x="506" y="352" />
      |        <di:waypoint xsi:type="dc:Point" x="506" y="316" />
      |        <bpmndi:BPMNLabel>
      |          <dc:Bounds x="521" y="345.5" width="0" height="13" />
      |        </bpmndi:BPMNLabel>
      |      </bpmndi:BPMNEdge>
      |      <bpmndi:BPMNShape id="Task_110o857_di" bpmnElement="Task_110o857">
      |        <dc:Bounds x="456" y="105" width="100" height="80" />
      |      </bpmndi:BPMNShape>
      |      <bpmndi:BPMNEdge id="SequenceFlow_1q524kw_di" bpmnElement="SequenceFlow_1q524kw">
      |        <di:waypoint xsi:type="dc:Point" x="506" y="236" />
      |        <di:waypoint xsi:type="dc:Point" x="506" y="211" />
      |        <di:waypoint xsi:type="dc:Point" x="506" y="211" />
      |        <di:waypoint xsi:type="dc:Point" x="506" y="185" />
      |        <bpmndi:BPMNLabel>
      |          <dc:Bounds x="521" y="204.5" width="0" height="13" />
      |        </bpmndi:BPMNLabel>
      |      </bpmndi:BPMNEdge>
      |      <bpmndi:BPMNShape id="EndEvent_116513k_di" bpmnElement="EndEvent_116513k">
      |        <dc:Bounds x="647" y="127" width="36" height="36" />
      |        <bpmndi:BPMNLabel>
      |          <dc:Bounds x="665" y="166" width="0" height="13" />
      |        </bpmndi:BPMNLabel>
      |      </bpmndi:BPMNShape>
      |      <bpmndi:BPMNEdge id="SequenceFlow_1ykmm13_di" bpmnElement="SequenceFlow_1ykmm13">
      |        <di:waypoint xsi:type="dc:Point" x="556" y="145" />
      |        <di:waypoint xsi:type="dc:Point" x="647" y="145" />
      |        <bpmndi:BPMNLabel>
      |          <dc:Bounds x="601.5" y="123.5" width="0" height="13" />
      |        </bpmndi:BPMNLabel>
      |      </bpmndi:BPMNEdge>
      |    </bpmndi:BPMNPlane>
      |  </bpmndi:BPMNDiagram>
      |</bpmn:definitions>
    """.stripMargin
  val dmnSchema =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<definitions xmlns="http://www.omg.org/spec/DMN/20151101/dmn.xsd" id="definitions_08agkzm" name="definitions" namespace="http://camunda.org/schema/1.0/dmn">
      |  <decision id="dish" name="Dish">
      |    <decisionTable id="decisionTable">
      |      <input id="input1" label="Season">
      |        <inputExpression id="inputExpression1" typeRef="string">
      |          <text></text>
      |        </inputExpression>
      |      </input>
      |      <input id="InputClause_0bvfj8f" label="How many guests">
      |        <inputExpression id="LiteralExpression_10krypc" typeRef="integer" />
      |      </input>
      |      <output id="output1" label="Dish" name="" typeRef="string" />
      |      <rule id="row-678150647-1">
      |        <inputEntry id="UnaryTests_1xur7xl">
      |          <text><![CDATA["Fall"]]></text>
      |        </inputEntry>
      |        <inputEntry id="UnaryTests_1kecr9s">
      |          <text><![CDATA[<= 8]]></text>
      |        </inputEntry>
      |        <outputEntry id="LiteralExpression_0ru79xu">
      |          <text><![CDATA["Spareribs"]]></text>
      |        </outputEntry>
      |      </rule>
      |      <rule id="row-678150647-2">
      |        <inputEntry id="UnaryTests_1pgiuzo">
      |          <text><![CDATA["Winter"]]></text>
      |        </inputEntry>
      |        <inputEntry id="UnaryTests_1xck9b0">
      |          <text><![CDATA[<= 8]]></text>
      |        </inputEntry>
      |        <outputEntry id="LiteralExpression_11znmeo">
      |          <text><![CDATA["Roastbeef"]]></text>
      |        </outputEntry>
      |      </rule>
      |      <rule id="row-678150647-3">
      |        <inputEntry id="UnaryTests_0fernuk">
      |          <text><![CDATA["Spring"]]></text>
      |        </inputEntry>
      |        <inputEntry id="UnaryTests_1uuff50">
      |          <text><![CDATA[<= 4]]></text>
      |        </inputEntry>
      |        <outputEntry id="LiteralExpression_0qmvk5j">
      |          <text><![CDATA["Dry Aged Gourmet Steak"]]></text>
      |        </outputEntry>
      |      </rule>
      |      <rule id="row-678150647-4">
      |        <description>Save money</description>
      |        <inputEntry id="UnaryTests_1hjx1o8">
      |          <text><![CDATA["Spring"]]></text>
      |        </inputEntry>
      |        <inputEntry id="UnaryTests_0s41nwz">
      |          <text>[5..8]</text>
      |        </inputEntry>
      |        <outputEntry id="LiteralExpression_15zaa89">
      |          <text><![CDATA["Steak"]]></text>
      |        </outputEntry>
      |      </rule>
      |      <rule id="row-678150647-5">
      |        <description>Less effort</description>
      |        <inputEntry id="UnaryTests_1vjd3o9">
      |          <text><![CDATA["Fall", "Winter", "Spring"]]></text>
      |        </inputEntry>
      |        <inputEntry id="UnaryTests_1tmaao4">
      |          <text><![CDATA[> 8]]></text>
      |        </inputEntry>
      |        <outputEntry id="LiteralExpression_14pf9mr">
      |          <text><![CDATA["Stew"]]></text>
      |        </outputEntry>
      |      </rule>
      |      <rule id="row-678150647-6">
      |        <description>Why not?</description>
      |        <inputEntry id="UnaryTests_02uodb3">
      |          <text><![CDATA["Summer"]]></text>
      |        </inputEntry>
      |        <inputEntry id="UnaryTests_1xi6y62">
      |          <text></text>
      |        </inputEntry>
      |        <outputEntry id="LiteralExpression_1hhk2m1">
      |          <text><![CDATA["Light Salad and nice Steak"]]></text>
      |        </outputEntry>
      |      </rule>
      |    </decisionTable>
      |  </decision>
      |</definitions>
    """.stripMargin

  val cmmnSchema = s"""|<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
       |<cmmn:definitions id="_d7e7cad4-86f1-4c04-9dff-a9aace3afb61"
       |  targetNamespace="http://cmmn.org" xmlns:cmmn="http://www.omg.org/spec/CMMN/20151109/MODEL"
       |  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:camunda="http://camunda.org/schema/1.0/cmmn">
       |  <cmmn:case id="loan_application">
       |    <cmmn:casePlanModel autoComplete="false"
       |      name="Loan Application" id="CasePlanModel">
       |      <!-- Plan Items -->
       |      <cmmn:planItem definitionRef="HumanTask_1" id="PI_HumanTask_1" />
       |      <cmmn:planItem definitionRef="HumanTask_2" id="PI_HumanTask_2">
       |        <cmmn:exitCriterion sentryRef="Sentry_2" />
       |      </cmmn:planItem>
       |      <cmmn:planItem definitionRef="Milestone_1" id="PI_Milestone_1">
       |        <cmmn:entryCriterion sentryRef="Sentry_1" />
       |      </cmmn:planItem>
       |
 |      <!-- Sentries -->
       |      <cmmn:sentry id="Sentry_1">
       |        <cmmn:planItemOnPart sourceRef="PI_HumanTask_1">
       |          <cmmn:standardEvent>complete</cmmn:standardEvent>
       |        </cmmn:planItemOnPart>
       |        <cmmn:planItemOnPart sourceRef="PI_HumanTask_2">
       |          <cmmn:standardEvent>complete</cmmn:standardEvent>
       |        </cmmn:planItemOnPart>
       |        <cmmn:ifPart>
       |          <cmmn:condition><![CDATA[$${applicationSufficient && rating > 3}]]></cmmn:condition>
       |        </cmmn:ifPart>
       |      </cmmn:sentry>
       |      <cmmn:sentry id="Sentry_2">
       |        <cmmn:planItemOnPart sourceRef="PI_HumanTask_1">
       |          <cmmn:standardEvent>complete</cmmn:standardEvent>
       |        </cmmn:planItemOnPart>
       |        <cmmn:ifPart>
       |          <cmmn:condition>$${!applicationSufficient}</cmmn:condition>
       |        </cmmn:ifPart>
       |      </cmmn:sentry>
       |
 |      <!-- Plan Item Definitions -->
       |      <cmmn:humanTask isBlocking="true" name="Check Application" id="HumanTask_1" camunda:assignee="demo">
       |        <cmmn:defaultControl>
       |          <cmmn:manualActivationRule>
       |            <cmmn:condition>$${false}</cmmn:condition>
       |          </cmmn:manualActivationRule>
       |        </cmmn:defaultControl>
       |      </cmmn:humanTask>
       |      <cmmn:humanTask isBlocking="true" name="Provide Customer Rating" id="HumanTask_2" camunda:assignee="demo">
       |        <cmmn:defaultControl>
       |          <cmmn:manualActivationRule>
       |            <cmmn:condition>$${false}</cmmn:condition>
       |          </cmmn:manualActivationRule>
       |        </cmmn:defaultControl>
       |      </cmmn:humanTask>
       |      <cmmn:milestone name="Approved" id="Milestone_1">
       |        <cmmn:extensionElements>
       |          <camunda:caseExecutionListener event="occur" class="org.camunda.bpm.getstarted.cmmn.loanapproval.LifecycleListener" />
       |        </cmmn:extensionElements>
       |      </cmmn:milestone>
       |    </cmmn:casePlanModel>
       |  </cmmn:case>
       |</cmmn:definitions>
     """.stripMargin

}
