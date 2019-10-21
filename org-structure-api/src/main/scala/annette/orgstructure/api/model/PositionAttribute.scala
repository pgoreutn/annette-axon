package annette.orgstructure.api.model

import play.api.libs.json.{Format, Json, JsonConfiguration, JsonNaming, OFormat}

sealed trait PositionAttribute {
  val value: String
}


case class AssignedPositionAttribute (value: String) extends PositionAttribute

object AssignedPositionAttribute {
  implicit val format: Format[AssignedPositionAttribute] = Json.format
}

case class InheritedPositionAttribute (value: String) extends PositionAttribute

object InheritedPositionAttribute {
  implicit val format: Format[InheritedPositionAttribute] = Json.format
}

object PositionAttribute {
  implicit val config = JsonConfiguration(discriminator = "positionAttributeType", typeNaming = JsonNaming { fullName =>
    fullName.split("\\.").toSeq.last
  })

  implicit val format = Json.format[PositionAttribute]
}

