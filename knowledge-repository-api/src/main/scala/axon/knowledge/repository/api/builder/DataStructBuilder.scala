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
      .filter(_.data match {
        case ObjectData(_, None, _) => true
        case ArrayData(_, None, _)  => true
        case _                      => false
      })
      .foldLeft(Future.successful(ds))((acc: Future[DataStructDef], item: DataItemDef) => {
        for {
          newDS <- acc
          newData <- item.data match {
            case ObjectData(key, None, struct) =>
              buildJsObject(key).map(jsObject => ObjectData(key, Some(jsObject), struct))
            case ArrayData(key, None, struct) =>
              Future.successful(ArrayData(key, Some(new JsArray()), struct))
          }
        } yield {
          val newItem = item.copy(data = newData)
          newDS.copy(items = newDS.items + (newItem.key -> newItem))
        }
      })
  }

  def buildJsObject(key: DataStructKey)(implicit ec: ExecutionContext): Future[JsObject] = {
    for {
      ds <- buildSingleLevelDef(key)
    } yield {
      val map = ds.items.values
        .map { e =>
          e.data match {
            case StringData(Some(value))       => Some(e.key -> new JsString(value))
            case IntData(Some(value))          => Some(e.key -> new JsNumber(value))
            case DoubleData(Some(value))       => Some(e.key -> new JsNumber(value))
            case DecimalData(Some(value))      => Some(e.key -> new JsNumber(value))
            case BooleanData(Some(value))      => Some(e.key -> JsBoolean(value))
            case DateData(Some(value))         => Some(e.key -> new JsString(value.toString))
            case ObjectData(_, Some(value), _) => Some(e.key -> value)
            case ArrayData(_, Some(value), _)  => Some(e.key -> value)
            case _                             => None
          }
        }
        .flatten
        .toMap
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
