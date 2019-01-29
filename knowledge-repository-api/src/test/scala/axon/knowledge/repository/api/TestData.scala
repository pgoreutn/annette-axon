package axon.knowledge.repository.api
import java.time.{OffsetDateTime, ZoneOffset}

import axon.knowledge.repository.api.model._
import play.api.libs.json.{JsArray, JsString}

object TestData {

  val addressDS = DataStructDef(
    "Address",
    "Addresss data structure definition",
    Some("Description of address data structure definition"),
    baseObjects = Seq("UpdatedBase", "VersionBase"),
    items = Seq(
      DataItemDef("zipCode", "Zip code", None, "Zip code", StringData(Some("123456"))),
      DataItemDef("country", "Country", None, "Country", StringData(Some("Russia"))),
      DataItemDef("region", "Region", None, "Region", StringData(Some("Moscow"))),
      DataItemDef("city", "City", None, "City", StringData(Some("Moscow"))),
      DataItemDef("addressLine", "Address Line", None, "Address Line", StringData(Some("Tverskaya, 20"))),
    ).map(e => e.key -> e).toMap
  )

  val person = DataStructDef(
    "Person",
    "Person",
    None,
    baseObjects = Seq.empty,
    items = Seq(
      DataItemDef("firstname", "Firstname", None, "Firstname", StringData(Some("Sergey"))),
      DataItemDef("lastname", "Lastname", None, "Lastname", StringData(Some("Esenin"))),
      DataItemDef("birthYear", "Birth Year", None, "Birth Year", IntData(Some(1895))),
      DataItemDef("birthdate", "Birth Date", None, "Birth Date", DateData(Some(OffsetDateTime.of(1895, 10, 3, 0, 0, 0, 0, ZoneOffset.ofHours(3))))),
      DataItemDef("salary", "Salary", None, "Salary", DecimalData(Some(BigDecimal(1234.56)))),
      DataItemDef("vip", "VIP", None, "VIP", BooleanData(Some(true))),
      DataItemDef("homeAddress", "Home Address", None, "Home Address", ObjectData("Address")),
      DataItemDef("workAddress", "Work Address", None, "Work Address", ObjectData("Address")),
      DataItemDef(
        "languages",
        "Languages",
        None,
        "Languages",
        ArrayData(StringData(),
                  Some(
                    new JsArray(
                      IndexedSeq(
                        JsString("Russian"),
                        JsString("English"),
                        JsString("French")
                      ))))
      ),
    ).map(e => e.key -> e).toMap
  )

  val createdBase = DataStructDef(
    "CreatedBase",
    "CreatedBase",
    None,
    baseObjects = Seq.empty,
    items = Seq(
      DataItemDef("createdBy", "CreatedBy", None, "CreatedBy", StringData(Some("Valery"))),
      DataItemDef("createdAt", "CreatedAt", None, "CreatedAt", DateData(Some(OffsetDateTime.now()))),
    ).map(e => e.key -> e).toMap
  )

  val updatedBase = DataStructDef(
    "UpdatedBase",
    "UpdatedBase",
    None,
    baseObjects = Seq("CreatedBase"),
    items = Seq(
      DataItemDef("updatedBy", "UpdatedBy", None, "UpdatedBy", StringData(Some("XXX"))),
      DataItemDef("updatedAt", "UpdatedAt", None, "UpdatedAt", DateData(Some(OffsetDateTime.now))),
    ).map(e => e.key -> e).toMap
  )

  val versionBase = DataStructDef(
    "VersionBase",
    "VersionBase",
    None,
    baseObjects = Seq.empty,
    items = Seq(
      DataItemDef("version", "Version", None, "Version", IntData(Some(0))),
    ).map(e => e.key -> e).toMap
  )

}
