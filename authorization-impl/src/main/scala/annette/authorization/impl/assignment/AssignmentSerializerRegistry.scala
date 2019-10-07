package annette.authorization.impl.assignment

import annette.authorization.api.model.{AuthorizationPrincipal, PrincipalAssignment}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

import scala.collection.immutable

object AssignmentSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] = List(
    JsonSerializer[AuthorizationPrincipal],
    JsonSerializer[PrincipalAssignment],
    JsonSerializer[AssignPrincipal],
    JsonSerializer[UnassignPrincipal],
    JsonSerializer[PrincipalAssigned],
    JsonSerializer[PrincipalUnassigned],
  )
}
