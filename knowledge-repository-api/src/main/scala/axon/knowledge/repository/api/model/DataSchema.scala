/***************************************************************************************
  * Copyright (c) 2014-2017 by Valery Lobachev
  * Redistribution and use in source and binary forms, with or without
  * modification, are NOT permitted without written permission from Valery Lobachev.
  *
  * Copyright (c) 2014-2017 Валерий Лобачев
  * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
  * запрещено без письменного разрешения правообладателя.
****************************************************************************************/
package axon.knowledge.repository.api.model
import play.api.libs.json.{Format, JsValue, Json}

case class DataSchemaSummary(
    key: DataSchemaKey,
    name: String,
    description: Option[String],
)

case class DataSchema(
    key: DataSchemaKey,
    name: String,
    description: Option[String],
    fields: Map[DataSchemaFieldKey, DataSchemaField],
) {
  def prettyPrint(level: Int = 0): String = {
    fields.values
      .map { f =>
        val prefix = "  " * level
        val postfix = f.datatype match {
          case RecordType(_, Some(ds)) =>
            s":\n${ds.prettyPrint(level + 1)}"
          case ArrayType(RecordType(_, Some(ds))) =>
            s":\n${ds.prettyPrint(level + 1)}"
          case _ => ""
        }
        s"$prefix${f.key}: ${datatypeToString(f.datatype)}${postfix}"
      }
      .mkString("\n")
  }

  private def datatypeToString(datatype: Datatype): String = {
    datatype match {
      case StringType()  => "String"
      case IntType()     => "Int"
      case DoubleType()  => "Double"
      case DecimalType() => "Decimal"
      case BooleanType() => "Boolean"
      case DateType()    => "Date"
      case b: RecordType => s"Record ${b.key}"
      case b: ArrayType  => s"Array of ${datatypeToString(b.element)}"
    }
  }
}

case class DataSchemaField(
    key: DataSchemaFieldKey,
    name: String,
    caption: String,
    datatype: Datatype,
    value: Option[JsValue],
)

case class DataValue(
    key: DataSchemaFieldKey,
    datatype: Datatype,
    value: Option[JsValue],
)

object DataSchemaSummary {
  implicit val format: Format[DataSchemaSummary] = Json.format
}

object DataSchema {
  implicit val format: Format[DataSchema] = Json.format
}

object DataSchemaField {
  implicit val format: Format[DataSchemaField] = Json.format
}

object DataValue {
  implicit val format: Format[DataValue] = Json.format
}
