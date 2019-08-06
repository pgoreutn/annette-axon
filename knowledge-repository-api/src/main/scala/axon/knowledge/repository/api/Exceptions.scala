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

package axon.knowledge.repository.api

import annette.shared.exceptions.{AnnetteException, AnnetteTransportException}
import com.lightbend.lagom.scaladsl.api.transport.TransportErrorCode

object DataSchemaNotFound {
  val ErrorCode = TransportErrorCode.NotFound
  val MessageCode = "knowledgeRepository.dataSchema.notFound"

  def apply(key: String) = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode, Map("id" -> key), Some(s"$MessageCode($key)"))
  )
}

object DataSchemasNotExist {
  val ErrorCode = TransportErrorCode.NotFound
  val MessageCode = "knowledgeRepository.dataSchema.notFound"

  def apply(keys: Seq[String]) = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode, Map("id" -> keys.mkString(", ")), Some(s"$MessageCode($keys)"))
  )

}

object DataSchemaAlreadyExist {
  val ErrorCode = TransportErrorCode.BadRequest
  val MessageCode = "knowledgeRepository.dataSchema.alreadyExist"

  def apply(id: String) = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode, Map("id" -> id), Some(s"$MessageCode($id)"))
  )

}

object KeyRequired {
  val ErrorCode = TransportErrorCode.BadRequest
  val MessageCode = "knowledgeRepository.dataSchema.keyRequired"

  def apply() = new AnnetteTransportException(
    ErrorCode,
    new AnnetteException(MessageCode)
  )

}
