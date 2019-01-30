package axon.knowledge.repository.api.builder
import axon.knowledge.repository.api.model._
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

trait DataStructDefFinder {
  def find(key: DataStructKey): Future[DataStructDef]
}
class DataStructBuilder(finder: DataStructDefFinder) {

  def buildSingleLevelDef(key: DataStructKey)(implicit ec: ExecutionContext): Future[DataStructDef] = {
    for {
      dataStructDef <- finder.find(key)
      result <- buildSingleLevelDef(dataStructDef)
    } yield result
  }

  def buildSingleLevelDef(ds: DataStructDef)(implicit ec: ExecutionContext): Future[DataStructDef] = {
    for {
      dsWithBase <- buildBase(ds)
      merged = mergeItems(dsWithBase, ds)
      initialized <- initialize(merged)
    } yield initialized
  }

  def initialize(ds: DataStructDef)(implicit ec: ExecutionContext): Future[DataStructDef] = {
    ds.items.values
      .filter(_ match {
        case DataItemDef(_, _, _, _, ObjectType(_, _), None) => true
        case DataItemDef(_, _, _, _, ArrayType(_, _), None)  => true
        case _                                               => false
      })
      .foldLeft(Future.successful(ds))((acc: Future[DataStructDef], item: DataItemDef) => {
        for {
          newDS <- acc
          newItem <- item match {
            case DataItemDef(_, _, _, _, ObjectType(key, _), None) =>
              buildJsObject(key).map(jsObject => item.copy(value = Some(jsObject)))
            case DataItemDef(_, _, _, _, ArrayType(key, _), None) =>
              Future.successful(item.copy(value = Some(new JsArray())))
          }
        } yield {
          newDS.copy(items = newDS.items + (newItem.key -> newItem))
        }
      })
  }

  def buildJsObject(key: DataStructKey)(implicit ec: ExecutionContext): Future[JsObject] = {
    for {
      ds <- buildSingleLevelDef(key)
    } yield {
      val map = ds.items.values.flatMap { e =>
        e match {
          case DataItemDef(_, _, _, _, StringType(), Some(value))     => Some(e.key -> value)
          case DataItemDef(_, _, _, _, IntType(), Some(value))        => Some(e.key -> value)
          case DataItemDef(_, _, _, _, DoubleType(), Some(value))     => Some(e.key -> value)
          case DataItemDef(_, _, _, _, DecimalType(), Some(value))    => Some(e.key -> value)
          case DataItemDef(_, _, _, _, BooleanType(), Some(value))    => Some(e.key -> value)
          case DataItemDef(_, _, _, _, DateType(), Some(value))       => Some(e.key -> value)
          case DataItemDef(_, _, _, _, ObjectType(_, _), Some(value)) => Some(e.key -> value)
          case DataItemDef(_, _, _, _, ArrayType(_, _), Some(value))  => Some(e.key -> value)
          case _                                                      => None
        }
      }.toMap
      new JsObject(map)
    }
  }

  def buildBase(dsd: DataStructDef)(implicit ec: ExecutionContext): Future[DataStructDef] = {
    val newDS = dsd.copy(baseObjects = Seq.empty, items = Map.empty)
    dsd.baseObjects.foldLeft(Future.successful(newDS))((acc: Future[DataStructDef], key: DataStructKey) => {
      for {
        a <- acc
        b <- buildSingleLevelDef(key)
      } yield mergeItems(a, b)
    })
  }

  def mergeItems(a: DataStructDef, b: DataStructDef) = {
    val items = a.items ++ b.items
    a.copy(items = items)
  }
}
