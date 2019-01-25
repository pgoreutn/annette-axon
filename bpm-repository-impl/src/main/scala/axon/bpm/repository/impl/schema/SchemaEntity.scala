/***************************************************************************************
  * Copyright (c) 2014-2017 by Valery Lobachev
  * Redistribution and use in source and binary forms, with or without
  * modification, are NOT permitted without written permission from Valery Lobachev.
  *
  * Copyright (c) 2014-2017 Валерий Лобачев
  * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
  * запрещено без письменного разрешения правообладателя.
****************************************************************************************/
package axon.bpm.repository.impl.schema

import akka.Done
import axon.bpm.repository.api.{NotationChangeProhibited, Schema, SchemaAlreadyExist, SchemaNotFound}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

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
        .onReadOnlyCommand[CreateSchema, Schema] {
          case (CreateSchema(Schema(id, _, _, _, _, _)), ctx, state) =>
            ctx.commandFailed(SchemaAlreadyExist(id))
          //ctx.commandFailed(NotFound(id))
        }
        .onCommand[UpdateSchema, Schema] {
          case (UpdateSchema(schema), ctx, state) =>
            // TODO: validate notation
            ctx.thenPersist(SchemaUpdated(schema))(_ => ctx.reply(schema))
        }
        .onCommand[DeleteSchema, Done] {
          case (DeleteSchema(id), ctx, state) =>
            ctx.thenPersist(SchemaDeleted(id))(_ => ctx.reply(Done))
        }
        .onEvent {
          case (SchemaUpdated(schema), state) =>
            state.map(s => s.copy(name = schema.name, description = schema.description, xml = schema.xml))
          case (SchemaDeleted(_), _) =>
            None
        }

    case None =>
      Actions()
        .onCommand[CreateSchema, Schema] {
          case (CreateSchema(schema), ctx, state) =>
            ctx.thenPersist(SchemaCreated(schema))(_ => ctx.reply(schema))
        }
        .onReadOnlyCommand[UpdateSchema, Schema] {
          case (UpdateSchema(Schema(id, _, _, _, _, _)), ctx, state) => ctx.commandFailed(SchemaNotFound(id))
        }
        .onReadOnlyCommand[DeleteSchema, Done] {
          case (DeleteSchema(id), ctx, state) => ctx.commandFailed(SchemaNotFound(id))
        }
        .onReadOnlyCommand[FindSchemaById, Option[Schema]] {
          case (FindSchemaById(_), ctx, state) => ctx.reply(None)
        }
        .onEvent {
          case (SchemaCreated(schema), state) =>
            Some(schema)
        }
  }

}
