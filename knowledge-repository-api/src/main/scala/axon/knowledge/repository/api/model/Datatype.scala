/*
 * Copyright 2018 Valery Lobachev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package axon.knowledge.repository.api.model

import play.api.libs.json._

sealed trait Datatype
case class StringType() extends Datatype
case class IntType() extends Datatype
case class DoubleType() extends Datatype
case class DecimalType() extends Datatype
case class BooleanType() extends Datatype
case class DateType() extends Datatype
case class RecordType(key: DataSchemaKey, struct: Option[DataSchema] = None) extends Datatype
case class ArrayType(element: Datatype) extends Datatype

object RecordType {
  implicit val format: Format[RecordType] = Json.format
}

object ArrayType {
  implicit val format: Format[ArrayType] = Json.format
}

object Datatype {
  implicit val format: Format[Datatype] = new Format[Datatype] {
    def reads(json: JsValue): JsResult[Datatype] = {
      json match {

        case data: JsObject if data.keys.contains("type") =>
          val type_ = data.value("type")
          type_ match {
            case JsString("string")  => JsSuccess(StringType())
            case JsString("int")     => JsSuccess(IntType())
            case JsString("double")  => JsSuccess(DoubleType())
            case JsString("decimal") => JsSuccess(DecimalType())
            case JsString("boolean") => JsSuccess(BooleanType())
            case JsString("date")    => JsSuccess(DateType())
            case JsString("record")  => Json.fromJson[RecordType](data)(RecordType.format)
            case JsString("array")   => Json.fromJson[ArrayType](data)(ArrayType.format)
            case _                   => JsError(s"Unknown class '$type_'")
          }
        case _ => JsError(s"Unexpected JSON value $json")
      }
    }

    def writes(datatype: Datatype): JsValue = {
      datatype match {
        case StringType()  => JsObject(Seq("type" -> JsString("string")))
        case IntType()     => JsObject(Seq("type" -> JsString("int")))
        case DoubleType()  => JsObject(Seq("type" -> JsString("double")))
        case DecimalType() => JsObject(Seq("type" -> JsString("decimal")))
        case BooleanType() => JsObject(Seq("type" -> JsString("boolean")))
        case DateType()    => JsObject(Seq("type" -> JsString("date")))
        case b: RecordType => Json.toJson(b)(RecordType.format).asInstanceOf[JsObject] + ("type" -> JsString("record"))
        case b: ArrayType  => Json.toJson(b)(ArrayType.format).asInstanceOf[JsObject] + ("type" -> JsString("array"))
      }
    }
  }
}
