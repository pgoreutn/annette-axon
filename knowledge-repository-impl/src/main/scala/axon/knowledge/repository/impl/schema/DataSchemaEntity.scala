/***************************************************************************************
  * Copyright (c) 2014-2017 by Valery Lobachev
  * Redistribution and use in source and binary forms, with or without
  * modification, are NOT permitted without written permission from Valery Lobachev.
  *
  * Copyright (c) 2014-2017 Валерий Лобачев
  * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
  * запрещено без письменного разрешения правообладателя.
****************************************************************************************/
package axon.knowledge.repository.impl.schema

import akka.Done
import axon.knowledge.repository.api.model.DataSchema
import axon.knowledge.repository.api.{DataSchemaAlreadyExist, DataSchemaNotFound}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

class DataSchemaEntity extends PersistentEntity {
  override type Command = DataSchemaCommand
  override type Event = DataSchemaEvent
  override type State = Option[DataSchema]
  override def initialState: State = None

  override def behavior: Behavior = {
    case Some(_) =>
      Actions()
        .onReadOnlyCommand[FindDataSchemaByKey, Option[DataSchema]] {
          case (FindDataSchemaByKey(key), ctx, state) => ctx.reply(state)
        }
        .onReadOnlyCommand[CreateDataSchema, DataSchema] {
          case (CreateDataSchema(DataSchema(key, _, _, _, _)), ctx, state) =>
            ctx.commandFailed(DataSchemaAlreadyExist(key))
        }
        .onCommand[UpdateDataSchema, DataSchema] {
          case (UpdateDataSchema(dataSchema), ctx, state) =>
            ctx.thenPersist(DataSchemaUpdated(dataSchema))(_ => ctx.reply(dataSchema))
        }
        .onCommand[DeleteDataSchema, Done] {
          case (DeleteDataSchema(key), ctx, state) =>
            ctx.thenPersist(DataSchemaDeleted(key))(_ => ctx.reply(Done))
        }
        .onEvent {
          case (DataSchemaUpdated(dataSchema), state) =>
            state.map(s =>
              s.copy(name = dataSchema.name, description = dataSchema.description, baseSchemas = dataSchema.baseSchemas, fields = dataSchema.fields))
          case (DataSchemaDeleted(_), _) =>
            None
        }

    case None =>
      Actions()
        .onCommand[CreateDataSchema, DataSchema] {
          case (CreateDataSchema(dataSchema), ctx, state) =>
            ctx.thenPersist(DataSchemaCreated(dataSchema))(_ => ctx.reply(dataSchema))
        }
        .onReadOnlyCommand[UpdateDataSchema, DataSchema] {
          case (UpdateDataSchema(DataSchema(key, _, _, _, _)), ctx, state) => ctx.commandFailed(DataSchemaNotFound(key))
        }
        .onReadOnlyCommand[DeleteDataSchema, Done] {
          case (DeleteDataSchema(key), ctx, state) => ctx.commandFailed(DataSchemaNotFound(key))
        }
        .onReadOnlyCommand[FindDataSchemaByKey, Option[DataSchema]] {
          case (FindDataSchemaByKey(_), ctx, state) => ctx.reply(None)
        }
        .onEvent {
          case (DataSchemaCreated(dataSchema), state) =>
            Some(dataSchema)
        }
  }

}
