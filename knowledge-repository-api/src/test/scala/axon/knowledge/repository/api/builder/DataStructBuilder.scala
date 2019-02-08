package axon.knowledge.repository.api.builder
import axon.knowledge.repository.api.model._
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

trait DataStructDefFinder {
  def find(key: DataSchemaKey): Future[DataSchema]
}
class DataStructBuilder(finder: DataStructDefFinder) {

  def buildSingleLevelDef(key: DataSchemaKey)(implicit ec: ExecutionContext): Future[DataSchema] = {
    for {
      dataStructDef <- finder.find(key)
      result <- buildSingleLevelDef(dataStructDef)
    } yield result
  }

  def buildSingleLevelDef(ds: DataSchema)(implicit ec: ExecutionContext): Future[DataSchema] = {
    for {
      dsWithBase <- buildBase(ds)
      merged = mergeItems(dsWithBase, ds)
      initialized <- initialize(merged)
    } yield initialized
  }

  def initialize(ds: DataSchema)(implicit ec: ExecutionContext): Future[DataSchema] = {
    ds.fields.values
      .filter(_ match {
        case DataSchemaField(_, _, _, RecordType(_, _), None) => true
        case DataSchemaField(_, _, _, ArrayType(_, _), None)  => true
        case _                                                => false
      })
      .foldLeft(Future.successful(ds))((acc: Future[DataSchema], item: DataSchemaField) => {
        for {
          newDS <- acc
          newItem <- item match {
            case DataSchemaField(_, _, _, RecordType(key, _), None) =>
              buildJsObject(key).map(jsObject => item.copy(value = Some(jsObject)))
            case DataSchemaField(_, _, _, ArrayType(key, _), None) =>
              Future.successful(item.copy(value = Some(new JsArray())))
          }
        } yield {
          newDS.copy(fields = newDS.fields + (newItem.key -> newItem))
        }
      })
  }

  def buildJsObject(key: DataSchemaKey)(implicit ec: ExecutionContext): Future[JsObject] = {
    for {
      ds <- buildSingleLevelDef(key)
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
          case DataSchemaField(_, _, _, ArrayType(_, _), Some(value))  => Some(e.key -> value)
          case _                                                       => None
        }
      }.toMap
      new JsObject(map)
    }
  }

  def buildBase(dsd: DataSchema)(implicit ec: ExecutionContext): Future[DataSchema] = {
    val newDS = dsd.copy(baseSchemas = Seq.empty, fields = Map.empty)
    dsd.baseSchemas.foldLeft(Future.successful(newDS))((acc: Future[DataSchema], key: DataSchemaKey) => {
      for {
        a <- acc
        b <- buildSingleLevelDef(key)
      } yield mergeItems(a, b)
    })
  }

  def mergeItems(a: DataSchema, b: DataSchema) = {
    val items = a.fields ++ b.fields
    a.copy(fields = items)
  }
}
