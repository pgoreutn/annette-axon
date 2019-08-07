package annette.person.repository.api.model

import annette.person.repository.api.model.PersonType.PersonType
import play.api.libs.json.{Format, Json}

case class PersonFindQuery(
    offset: Int,
    size: Int,
    filter: Option[String], //search by filter in person's names, email and phone
    lastname: Option[String], //search in last name of the person
    firstname: Option[String], //search in first name
    middlename: Option[String], //search in middle name
    personType: Option[PersonType], //search in type of person: user or contact
    phone: Option[String], //search in phone
    email: Option[String], //search in email
    activeOnly: Boolean, //search active persons only (by default)
    sortBy: Option[String] //sort results by field provided
)

object PersonFindQuery {
  implicit val format: Format[PersonFindQuery] = Json.format
}