package annette.orgstructure.api.model

import play.api.libs.json.{Format, Json, JsonConfiguration, JsonNaming}

sealed trait UnitAttribute {
  val value: String
}

case class AssignedUnitAttribute (value: String) extends UnitAttribute
case class AssignedHeritableUnitAttribute (value: String) extends UnitAttribute
case class InheritedUnitAttribute (value: String) extends UnitAttribute


object AssignedUnitAttribute {
  implicit val format: Format[AssignedUnitAttribute] = Json.format
}

object AssignedHeritableUnitAttribute {
  implicit val format: Format[AssignedHeritableUnitAttribute] = Json.format
}

object InheritedUnitAttribute {
  implicit val format: Format[InheritedUnitAttribute] = Json.format
}


object UnitAttribute {
  implicit val config = JsonConfiguration(discriminator = "unitAttributeType", typeNaming = JsonNaming { fullName =>
    fullName.split("\\.").toSeq.last
  })

  implicit val format = Json.format[UnitAttribute]
}
