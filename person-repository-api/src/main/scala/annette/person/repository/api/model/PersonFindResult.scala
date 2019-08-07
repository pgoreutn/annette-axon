package annette.person.repository.api.model

import java.time.OffsetDateTime

import play.api.libs.json.{Format, Json}

case class PersonFindResult(
    total: Int, // total items in query
    hits: Seq[PersonHitResult] // results of search
)

object PersonFindResult {
  implicit val format: Format[PersonFindResult] = Json.format
}

case class PersonHitResult(
    id: PersonId, // person id
    score: Float, // store of this hit
    updatedAt: OffsetDateTime // date/time of last update
)
object PersonHitResult {
  implicit val format: Format[PersonHitResult] = Json.format
}
