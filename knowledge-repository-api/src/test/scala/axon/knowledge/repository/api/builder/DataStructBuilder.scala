package axon.knowledge.repository.api.builder
import axon.knowledge.repository.api.model._
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

trait DataStructDefFinder {
  def find(key: DataSchemaKey)(implicit ec: ExecutionContext): Future[DataSchema]
}
class DataStructBuilder(finder: DataStructDefFinder) {

  def buildSingleLevelDef(key: DataSchemaKey)(implicit ec: ExecutionContext): Future[DataSchema] = {
//    println(s"buildSingleLevelDef( $key )")

    for {
      dataStructDef <- finder.find(key)
      result <- buildSingleLevelDef(dataStructDef)
    } yield result
  }

  def buildSingleLevelDef(ds: DataSchema)(implicit ec: ExecutionContext): Future[DataSchema] = {
    ds.fields.values
      .filter(_ match {
        case DataSchemaField(_, _, _, RecordType(_, _), Some(_)) =>
          true
        case DataSchemaField(_, _, _, ArrayType(_), None) => true
        case _                                            => false
      })
      .foldLeft(Future.successful(ds))((acc: Future[DataSchema], item: DataSchemaField) => {
        for {
          newDS <- acc
          newItem <- item match {
            case DataSchemaField(_, _, _, RecordType(key, _), Some(JsObject(map))) if map.isEmpty =>
              buildJsObject(key).map(jsObject => item.copy(value = Some(jsObject)))
            case DataSchemaField(_, _, _, RecordType(key, _), Some(_)) =>
              Future.successful(item)
            case DataSchemaField(_, _, _, ArrayType(key), None) =>
              Future.successful(item.copy(value = Some(new JsArray())))
          }
        } yield {
          newDS.copy(fields = newDS.fields + (newItem.key -> newItem))
        }
      })
  }

  def buildMultiLevelDef(key: DataSchemaKey)(implicit ec: ExecutionContext): Future[DataSchema] = {
//    println(s"buildMultiLevelDef( $key )")
    for {
      dataStructDef <- buildSingleLevelDef(key)
      result <- buildMultiLevelDef(dataStructDef)
    } yield result
  }

  def buildMultiLevelDef(ds: DataSchema)(implicit ec: ExecutionContext): Future[DataSchema] = {
//    println(s"buildMultiLevelDef2( ${ds.key} )")

    ds.fields.values
      .filter(_ match {
        case DataSchemaField(_, _, _, RecordType(_, _), _)            => true
        case DataSchemaField(_, _, _, ArrayType(RecordType(_, _)), _) => true
        case _                                                        => false
      })
      .foldLeft(Future.successful(ds))((acc: Future[DataSchema], item: DataSchemaField) => {
        for {
          newDS <- acc
          newItem <- item match {
            case DataSchemaField(_, _, _, RecordType(key, _), _) =>
              buildMultiLevelDef(key).map(nds => item.copy(datatype = RecordType(key, Some(nds))))
            case DataSchemaField(_, _, _, ArrayType(RecordType(key, _)), _) =>
              buildMultiLevelDef(key).map(nds => item.copy(datatype = ArrayType(RecordType(key, Some(nds)))))
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
          case DataSchemaField(_, _, _, ArrayType(_), Some(value))     => Some(e.key -> value)

          case DataSchemaField(_, _, _, _, None) => Some(e.key -> JsNull)

          case _ => None
        }
      }.toMap
      new JsObject(map)
    }
  }

}
