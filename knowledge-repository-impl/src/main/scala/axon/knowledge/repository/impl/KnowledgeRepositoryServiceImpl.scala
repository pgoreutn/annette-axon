package axon.knowledge.repository.impl

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.{Done, NotUsed}
import axon.knowledge.repository.api._
import axon.knowledge.repository.api.model._
import axon.knowledge.repository.impl.schema._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import play.api.libs.json.{JsArray, JsNull, JsObject}

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * Implementation of the KnowledgeService.
  */
class KnowledgeRepositoryServiceImpl(registry: PersistentEntityRegistry, system: ActorSystem, dataSchemaRepository: DataSchemaRepository)(
    implicit ec: ExecutionContext,
    mat: Materializer)
    extends KnowledgeRepositoryService {

  override def createDataSchema: ServiceCall[DataSchema, DataSchema] = ServiceCall { dataSchema =>
    refFor(dataSchema.key)
      .ask(CreateDataSchema(dataSchema))
  }

  override def updateDataSchema: ServiceCall[DataSchema, DataSchema] = ServiceCall { dataSchema =>
    refFor(dataSchema.key)
      .ask(UpdateDataSchema(dataSchema))
  }

  override def deleteDataSchema(key: DataSchemaKey): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    refFor(key).ask(DeleteDataSchema(key))
  }
  override def findDataSchemaByKey(key: String): ServiceCall[NotUsed, DataSchema] = ServiceCall { _ =>
    findDSByKey(key)
  }
  override def findDataSchema: ServiceCall[String, immutable.Seq[DataSchemaSummary]] = ServiceCall { filter =>
    // TODO: temporary solution, should be implemented using ElasticSearch
    dataSchemaRepository.findDataSchemas(filter.trim)
  }

  override def findDataSchemaByKeys: ServiceCall[immutable.Seq[DataSchemaKey], immutable.Seq[DataSchemaSummary]] = { keys =>
    dataSchemaRepository.findDataSchemaByKeys(keys.filter(_.trim.nonEmpty))
  }

  override def buildSingleLevel(key: String): ServiceCall[NotUsed, DataSchema] = ServiceCall { _ =>
    buildSingleLevelDS(key)
  }

  override def buildMultiLevel(key: String): ServiceCall[NotUsed, DataSchema] = ServiceCall { _ =>
    buildMultiLevelDS(key)
  }

  def findDSByKey(key: String): Future[DataSchema] = {
    refFor(key).ask(FindDataSchemaByKey(key)).map {
      case Some(dataSchema) => dataSchema
      case None             => throw DataSchemaNotFound(key)
    }
  }
  def buildSingleLevelDS(key: String): Future[DataSchema] = {
    for {
      dataStructDef <- findDSByKey(key)
      result <- buildSingleLevelDS(dataStructDef)
    } yield result
  }

  def buildSingleLevelDS(ds: DataSchema)(implicit ec: ExecutionContext): Future[DataSchema] = {
    ds.fields.values
      .filter(_ match {
        // make sure that cases below are aligned to foldLeft cases
        case DataSchemaField(_, _, _, RecordType(_, _), Some(_)) => true
        case DataSchemaField(_, _, _, ArrayType(_), None)        => true
        case _                                                   => false
      })
      .foldLeft(Future.successful(ds))((acc: Future[DataSchema], item: DataSchemaField) => {
        for {
          newDS <- acc
          newItem <- item match {
            // make sure that cases below are aligned to filter cases
            case DataSchemaField(_, _, _, RecordType(key, _), Some(JsObject(map))) if map.isEmpty =>
              buildJsObject(key).map(jsObject => item.copy(value = Some(jsObject)))
            case DataSchemaField(_, _, _, RecordType(_, _), Some(_)) =>
              Future.successful(item)
            case DataSchemaField(_, _, _, ArrayType(key), None) =>
              Future.successful(item.copy(value = Some(new JsArray())))
          }
        } yield {
          newDS.copy(fields = newDS.fields + (newItem.key -> newItem))
        }
      })
  }

  def buildJsObject(key: DataSchemaKey)(implicit ec: ExecutionContext): Future[JsObject] = {
    for {
      ds <- buildSingleLevelDS(key)
    } yield {
      val map = ds.fields.values.flatMap { e =>
        e match {
          case DataSchemaField(_, _, _, StringType(), Some(value))     => Some(e.key -> value)
          case DataSchemaField(_, _, _, IntType(), Some(value))        => Some(e.key -> value)
          case DataSchemaField(_, _, _, DoubleType(), Some(value))     => Some(e.key -> value)
          case DataSchemaField(_, _, _, DecimalType(), Some(value))    => Some(e.key -> value)
          case DataSchemaField(_, _, _, BooleanType(), Some(value))    => Some(e.key -> value)
          case DataSchemaField(_, _, _, DateType(), Some(value))       => Some(e.key -> value)
          case DataSchemaField(_, _, _, RecordType(_, _), Some(value)) => Some(e.key -> value)
          case DataSchemaField(_, _, _, ArrayType(_), Some(value))     => Some(e.key -> value)

          case DataSchemaField(_, _, _, _, None) => Some(e.key -> JsNull)

          case _ => None
        }
      }.toMap
      new JsObject(map)
    }
  }

  def buildMultiLevelDS(key: DataSchemaKey)(implicit ec: ExecutionContext): Future[DataSchema] = {
    //    println(s"buildMultiLevelDef( $key )")
    for {
      dataStructDef <- buildSingleLevelDS(key)
      result <- buildMultiLevelDS(dataStructDef)
    } yield result
  }

  def buildMultiLevelDS(ds: DataSchema)(implicit ec: ExecutionContext): Future[DataSchema] = {

    ds.fields.values
      .filter(_ match {
        // make sure that cases below are aligned to foldLeft cases
        case DataSchemaField(_, _, _, RecordType(_, _), _)            => true
        case DataSchemaField(_, _, _, ArrayType(RecordType(_, _)), _) => true
        case _                                                        => false
      })
      .foldLeft(Future.successful(ds))((acc: Future[DataSchema], item: DataSchemaField) => {
        for {
          newDS <- acc
          newItem <- item match {
            // make sure that cases below are aligned to filter cases
            case DataSchemaField(_, _, _, RecordType(key, _), _) =>
              buildMultiLevelDS(key).map(nds => item.copy(datatype = RecordType(key, Some(nds))))
            case DataSchemaField(_, _, _, ArrayType(RecordType(key, _)), _) =>
              buildMultiLevelDS(key).map(nds => item.copy(datatype = ArrayType(RecordType(key, Some(nds)))))
          }
        } yield {
          newDS.copy(fields = newDS.fields + (newItem.key -> newItem))
        }
      })
  }

  private def refFor(key: DataSchemaKey) = {
    if (key.trim.length != 0) {
      registry.refFor[DataSchemaEntity](key)
    } else {
      throw KeyRequired()
    }
  }
}
