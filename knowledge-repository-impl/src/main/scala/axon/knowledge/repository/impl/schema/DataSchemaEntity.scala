/*
 * Copyright 2018 Valery Lobachev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
          case (CreateDataSchema(DataSchema(key, _, _, _)), ctx, state) =>
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
            state.map(s => s.copy(name = dataSchema.name, description = dataSchema.description, fields = dataSchema.fields))
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
          case (UpdateDataSchema(DataSchema(key, _, _, _)), ctx, state) => ctx.commandFailed(DataSchemaNotFound(key))
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
