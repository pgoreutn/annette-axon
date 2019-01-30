package axon.knowledge.repository.api.model

import play.api.libs.json._

sealed trait Datatype
case class StringType() extends Datatype
case class IntType() extends Datatype
case class DoubleType() extends Datatype
case class DecimalType() extends Datatype
case class BooleanType() extends Datatype
case class DateType() extends Datatype
case class ObjectType(key: DataStructKey, struct: Option[DataStructDef] = None) extends Datatype
case class ArrayType(element: Datatype, struct: Option[DataStructDef] = None) extends Datatype

object ObjectType {
  implicit val format: Format[ObjectType] = Json.format
}

object ArrayType {
  implicit val format: Format[ArrayType] = Json.format
}

object Datatype {
  implicit val format: Format[Datatype] = new Format[Datatype] {
    def reads(json: JsValue): JsResult[Datatype] = {
      json match {
        case data: JsObject if data.keys.contains("$type") =>
          val type_ = data.value("$type")
          type_ match {
            case JsString("String")  => JsSuccess(StringType())
            case JsString("Int")     => JsSuccess(IntType())
            case JsString("Double")  => JsSuccess(DoubleType())
            case JsString("Decimal") => JsSuccess(DecimalType())
            case JsString("Boolean") => JsSuccess(BooleanType())
            case JsString("Date")    => JsSuccess(DateType())
            case JsString("Object")  => Json.fromJson[ObjectType](data)(ObjectType.format)
            case JsString("Array")   => Json.fromJson[ArrayType](data)(ArrayType.format)
            case _                   => JsError(s"Unknown class '$type_'")
          }
        case _ => JsError(s"Unexpected JSON value $json")
      }
    }

    def writes(datatype: Datatype): JsValue = {
      datatype match {
        case StringType()  => JsObject(Seq("$type" -> JsString("String")))
        case IntType()     => JsObject(Seq("$type" -> JsString("Int")))
        case DoubleType()  => JsObject(Seq("$type" -> JsString("Double")))
        case DecimalType() => JsObject(Seq("$type" -> JsString("Decimal")))
        case BooleanType() => JsObject(Seq("$type" -> JsString("Boolean")))
        case DateType()    => JsObject(Seq("$type" -> JsString("Date")))
        case b: ObjectType => Json.toJson(b)(ObjectType.format).asInstanceOf[JsObject] + ("$type" -> JsString("Object"))
        case b: ArrayType  => Json.toJson(b)(ArrayType.format).asInstanceOf[JsObject] + ("$type" -> JsString("Array"))
      }
    }
  }
}
