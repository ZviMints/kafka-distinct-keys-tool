package services

import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.ConsumerRecord

import scala.collection.mutable

object StorageService extends LazyLogging {

  case class UpdateResponse(key: String, isTombstone: Boolean)

  private[this] val _compacted: mutable.Set[String] = scala.collection.mutable.HashSet[String]()

  def updateDistinctKeys(kafkaMessage: ConsumerRecord[String, _]): UpdateResponse = {
    val key = kafkaMessage.key
    val value = Option(kafkaMessage.value)
    val isTombstone = value.isEmpty
    if(isTombstone) {
      _compacted.remove(key)
    }
    else {
      _compacted.addOne(key)
    }
    UpdateResponse(key, isTombstone)
  }

  def getDistinctKeys: Int = _compacted.size
}

