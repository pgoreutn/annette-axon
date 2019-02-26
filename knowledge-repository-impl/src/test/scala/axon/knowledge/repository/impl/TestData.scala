package axon.knowledge.repository.impl

import java.time.{OffsetDateTime, ZoneOffset}

import axon.knowledge.repository.api.model._
import play.api.libs.json._

import scala.util.Random

object TestData {

  def addressDS(
      key: String = s"Address-${Random.nextInt(9999).toString}",
      name: String = "Addresss data structure definition",
      description: String = "Description of address data structure definition") = DataSchema(
    key,
    name,
    Some(description),
    fields = Seq(
      DataSchemaField("zipCode", "Zip code", "Zip code", StringType(), Some(JsString("123456"))),
      DataSchemaField("country", "Country", "Country", StringType(), Some(JsString("Russia"))),
      DataSchemaField("region", "Region", "Region", StringType(), Some(JsString("Moscow"))),
      DataSchemaField("city", "City", "City", StringType(), Some(JsString("Moscow"))),
      DataSchemaField("addressLine", "Address Line", "Address Line", StringType(), Some(JsString("Tverskaya, 20"))),
      DataSchemaField("array", "array", "array", ArrayType(RecordType("VersionBase")), Some(JsArray())),
      DataSchemaField("record", "record", "record", RecordType("CreatedBase"), Some(JsObject(Seq.empty))),
    ).map(e => e.key -> e).toMap
  )

  val person = DataSchema(
    "Person",
    "Person",
    None,
    fields = Seq(
      DataSchemaField("firstname", "Firstname", "Firstname", StringType(), Some(JsString("Sergey"))),
      DataSchemaField("lastname", "Lastname", "Lastname", StringType(), Some(JsString("Esenin"))),
      DataSchemaField("birthYear", "Birth Year", "Birth Year", IntType(), Some(JsNumber(1895))),
      DataSchemaField("birthdate",
                      "Birth Date",
                      "Birth Date",
                      DateType(),
                      Some(JsString(OffsetDateTime.of(1895, 10, 3, 0, 0, 0, 0, ZoneOffset.ofHours(3)).toString))),
      DataSchemaField("salary", "Salary", "Salary", DecimalType(), Some(JsNumber(1234.56))),
      DataSchemaField("vip", "VIP", "VIP", BooleanType(), Some(JsBoolean(true))),
      DataSchemaField("homeAddress", "Home Address", "Home Address", RecordType("Address"), Some(JsObject(Seq.empty))),
      DataSchemaField("workAddress", "Work Address", "Work Address", RecordType("Address"), None),
      DataSchemaField(
        "languages",
        "Languages",
        "Languages",
        ArrayType(StringType()),
        Some(
          new JsArray(
            IndexedSeq(
              JsString("Russian"),
              JsString("English"),
              JsString("French")
            )))
      ),
      DataSchemaField("addresses", "Addresses", "Addresses", ArrayType(RecordType("Address")), None),
    ).map(e => e.key -> e).toMap
  )

  val createdBase = DataSchema(
    "CreatedBase",
    "CreatedBase",
    None,
    fields = Seq(
      DataSchemaField("createdBy", "CreatedBy", "CreatedBy", StringType(), Some(JsString("Valery"))),
      DataSchemaField("createdAt", "CreatedAt", "CreatedAt", DateType(), Some(JsString(OffsetDateTime.now().toString))),
    ).map(e => e.key -> e).toMap
  )

  val updatedBase = DataSchema(
    "UpdatedBase",
    "UpdatedBase",
    None,
    fields = Seq(
      DataSchemaField("updatedBy", "UpdatedBy", "UpdatedBy", StringType(), Some(JsString("XXX"))),
      DataSchemaField("updatedAt", "UpdatedAt", "UpdatedAt", DateType(), Some(JsString(OffsetDateTime.now.toString))),
    ).map(e => e.key -> e).toMap
  )

  val versionBase = DataSchema(
    "VersionBase",
    "VersionBase",
    None,
    fields = Seq(
      DataSchemaField("version", "Version", "Version", IntType(), Some(JsNumber(0))),
    ).map(e => e.key -> e).toMap
  )

}
