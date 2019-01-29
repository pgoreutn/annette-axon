package axon.knowledge.repository.api.model
import java.time.OffsetDateTime

import play.api.libs.json._

sealed trait Data {
  val value: Option[Any]
}

sealed trait Primitive extends Data
sealed trait Complex extends Data
sealed trait Collection extends Data {
  val element: Data
}

case class StringData(value: Option[String] = None) extends Primitive
object StringData {
  implicit val format: Format[StringData] = Json.format
}

case class IntData(value: Option[Int] = None) extends Primitive
object IntData {
  implicit val format: Format[IntData] = Json.format
}
case class DoubleData(value: Option[Double] = None) extends Primitive
object DoubleData {
  implicit val format: Format[DoubleData] = Json.format
}

case class DecimalData(value: Option[BigDecimal] = None) extends Primitive
object DecimalData {
  implicit val format: Format[DecimalData] = Json.format
}
case class BooleanData(value: Option[Boolean] = None) extends Primitive
object BooleanData {
  implicit val format: Format[BooleanData] = Json.format
}
case class DateData(value: Option[OffsetDateTime] = None) extends Primitive
object DateData {
  implicit val format: Format[DateData] = Json.format
}

case class ObjectData(key: DataStructKey, value: Option[JsObject] = None, struct: Option[DataStructDef] = None) extends Complex
object ObjectData {
  implicit val format: Format[ObjectData] = Json.format
}

case class ArrayData(element: Data, value: Option[JsArray] = None, struct: Option[DataStructDef] = None) extends Complex
object ArrayData {
  implicit val format: Format[ArrayData] = Json.format
}

object Data {
  implicit val format: Format[Data] = new Format[Data] {
    def reads(json: JsValue): JsResult[Data] = {
      json match {
        case data: JsObject if data.keys.contains("$type") =>
          val type_ = data.value("$type")
          type_ match {
            case JsString("String")  => Json.fromJson[StringData](data)(StringData.format)
            case JsString("Int")     => Json.fromJson[IntData](data)(IntData.format)
            case JsString("Double")  => Json.fromJson[DoubleData](data)(DoubleData.format)
            case JsString("Decimal") => Json.fromJson[DecimalData](data)(DecimalData.format)
            case JsString("Boolean") => Json.fromJson[BooleanData](data)(BooleanData.format)
            case JsString("Date")    => Json.fromJson[DateData](data)(DateData.format)
            case JsString("Object")  => Json.fromJson[ObjectData](data)(ObjectData.format)
            case JsString("Array")   => Json.fromJson[ArrayData](data)(ArrayData.format)
            case _                   => JsError(s"Unknown class '$type_'")
          }

        case _ => JsError(s"Unexpected JSON value $json")
      }
    }

    def writes(foo: Data): JsValue = {
      val (type_, sub) = foo match {
        case b: StringData  => ("String", Json.toJson(b)(StringData.format).asInstanceOf[JsObject])
        case b: IntData     => ("Int", Json.toJson(b)(IntData.format).asInstanceOf[JsObject])
        case b: DoubleData  => ("Double", Json.toJson(b)(DoubleData.format).asInstanceOf[JsObject])
        case b: DecimalData => ("Decimal", Json.toJson(b)(DecimalData.format).asInstanceOf[JsObject])
        case b: BooleanData => ("Boolean", Json.toJson(b)(BooleanData.format).asInstanceOf[JsObject])
        case b: DateData    => ("Date", Json.toJson(b)(DateData.format).asInstanceOf[JsObject])
        case b: ObjectData  => ("Object", Json.toJson(b)(ObjectData.format).asInstanceOf[JsObject])
        case b: ArrayData   => ("Array", Json.toJson(b)(ArrayData.format).asInstanceOf[JsObject])
      }
      sub + ("$type" -> JsString(type_))
    }
  }
}
