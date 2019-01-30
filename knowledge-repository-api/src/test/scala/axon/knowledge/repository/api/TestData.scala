package axon.knowledge.repository.api
import java.time.{OffsetDateTime, ZoneOffset}

import axon.knowledge.repository.api.model._
import play.api.libs.json.{JsArray, JsBoolean, JsNumber, JsString}

object TestData {

  val addressDS = DataStructDef(
    "Address",
    "Addresss data structure definition",
    Some("Description of address data structure definition"),
    baseObjects = Seq("UpdatedBase", "VersionBase"),
    items = Seq(
      DataItemDef("zipCode", "Zip code", None, "Zip code", StringType(), Some(JsString("123456"))),
      DataItemDef("country", "Country", None, "Country", StringType(), Some(JsString("Russia"))),
      DataItemDef("region", "Region", None, "Region", StringType(), Some(JsString("Moscow"))),
      DataItemDef("city", "City", None, "City", StringType(), Some(JsString("Moscow"))),
      DataItemDef("addressLine", "Address Line", None, "Address Line", StringType(), Some(JsString("Tverskaya, 20"))),
    ).map(e => e.key -> e).toMap
  )

  val person = DataStructDef(
    "Person",
    "Person",
    None,
    baseObjects = Seq.empty,
    items = Seq(
      DataItemDef("firstname", "Firstname", None, "Firstname", StringType(), Some(JsString("Sergey"))),
      DataItemDef("lastname", "Lastname", None, "Lastname", StringType(), Some(JsString("Esenin"))),
      DataItemDef("birthYear", "Birth Year", None, "Birth Year", IntType(), Some(JsNumber(1895))),
      DataItemDef("birthdate",
                  "Birth Date",
                  None,
                  "Birth Date",
                  DateType(),
                  Some(JsString(OffsetDateTime.of(1895, 10, 3, 0, 0, 0, 0, ZoneOffset.ofHours(3)).toString))),
      DataItemDef("salary", "Salary", None, "Salary", DecimalType(), Some(JsNumber(1234.56))),
      DataItemDef("vip", "VIP", None, "VIP", BooleanType(), Some(JsBoolean(true))),
      DataItemDef("homeAddress", "Home Address", None, "Home Address", ObjectType("Address"), None),
      DataItemDef("workAddress", "Work Address", None, "Work Address", ObjectType("Address"), None),
      DataItemDef(
        "languages",
        "Languages",
        None,
        "Languages",
        ArrayType(StringType()),
        Some(
          new JsArray(
            IndexedSeq(
              JsString("Russian"),
              JsString("English"),
              JsString("French")
            )))
      )
    ).map(e => e.key -> e).toMap
  )

  val createdBase = DataStructDef(
    "CreatedBase",
    "CreatedBase",
    None,
    baseObjects = Seq.empty,
    items = Seq(
      DataItemDef("createdBy", "CreatedBy", None, "CreatedBy", StringType(), Some(JsString("Valery"))),
      DataItemDef("createdAt", "CreatedAt", None, "CreatedAt", DateType(), Some(JsString(OffsetDateTime.now().toString))),
    ).map(e => e.key -> e).toMap
  )

  val updatedBase = DataStructDef(
    "UpdatedBase",
    "UpdatedBase",
    None,
    baseObjects = Seq("CreatedBase"),
    items = Seq(
      DataItemDef("updatedBy", "UpdatedBy", None, "UpdatedBy", StringType(), Some(JsString("XXX"))),
      DataItemDef("updatedAt", "UpdatedAt", None, "UpdatedAt", DateType(), Some(JsString(OffsetDateTime.now.toString))),
    ).map(e => e.key -> e).toMap
  )

  val versionBase = DataStructDef(
    "VersionBase",
    "VersionBase",
    None,
    baseObjects = Seq.empty,
    items = Seq(
      DataItemDef("version", "Version", None, "Version", IntType(), Some(JsString("0"))),
    ).map(e => e.key -> e).toMap
  )

}
