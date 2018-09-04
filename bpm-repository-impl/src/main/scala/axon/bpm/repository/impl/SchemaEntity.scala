/***************************************************************************************
  * Copyright (c) 2014-2017 by Valery Lobachev
  * Redistribution and use in source and binary forms, with or without
  * modification, are NOT permitted without written permission from Valery Lobachev.
  *
  * Copyright (c) 2014-2017 Валерий Лобачев
  * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
  * запрещено без письменного разрешения правообладателя.
****************************************************************************************/
package axon.bpm.repository.impl
import akka.Done
import axon.bpm.repository.api.{Schema, SchemaAlreadyExist, SchemaId, SchemaNotFound}
import com.lightbend.lagom.scaladsl.api.transport._
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.{Format, Json}

class SchemaEntity extends PersistentEntity {
  override type Command = SchemaCommand
  override type Event = SchemaEvent
  override type State = Option[Schema]
  override def initialState = None

  override def behavior: Behavior = {
    case Some(_) =>
      Actions()
        .onReadOnlyCommand[FindSchemaById, Option[Schema]] {
          case (FindSchemaById(id), ctx, state) => ctx.reply(state)
        }
        .onReadOnlyCommand[CreateSchema, Done] {
          case (CreateSchema(id, _, _, _, _), ctx, state) =>
            ctx.commandFailed(SchemaAlreadyExist(id))
          //ctx.commandFailed(NotFound(id))
        }
        .onCommand[UpdateSchema, Done] {
          case (UpdateSchema(id, name, description, schema), ctx, state) =>
            ctx.thenPersist(SchemaUpdated(id, name, description, schema))(_ => ctx.reply(Done))
        }
        .onCommand[DeleteSchema, Done] {
          case (DeleteSchema(id), ctx, state) =>
            ctx.thenPersist(SchemaDeleted(id))(_ => ctx.reply(Done))
        }
        .onEvent {
          case (SchemaUpdated(_, name, description, schema), state) =>
            state.map(s => s.copy(name = name, description = description, schema = schema))
          case (SchemaDeleted(_), _) =>
            None
        }

    case None =>
      Actions()
        .onCommand[CreateSchema, Done] {
          case (CreateSchema(id, name, description, notation, schema), ctx, state) =>
            ctx.thenPersist(SchemaCreated(id, name, description, notation, schema))(_ => ctx.reply(Done))
        }
        .onReadOnlyCommand[UpdateSchema, Done] {
          case (UpdateSchema(id, _, _, _), ctx, state) => ctx.commandFailed(SchemaNotFound(id))

        }
        .onReadOnlyCommand[DeleteSchema, Done] {
          case (DeleteSchema(id), ctx, state) => ctx.commandFailed(SchemaNotFound(id))
        }
        .onReadOnlyCommand[FindSchemaById, Option[Schema]] {
          case (FindSchemaById(_), ctx, state) => ctx.reply(None)
        }
        .onEvent {
          case (SchemaCreated(id, name, description, notation, schema), state) =>
            Some(Schema(id, name, description, notation, schema))
        }
  }

}
