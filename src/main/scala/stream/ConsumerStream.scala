package stream

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ClosedShape
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink}
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import services.StorageService
import services.StorageService.UpdateResponse
import stream.ConsumerStream.{TombstonesMessages, TotalMessages}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{Future, TimeoutException}

class ConsumerStream(bootstrapAddress: String, idleTimeout: FiniteDuration, topic: String)(implicit val system: ActorSystem) extends LazyLogging {

  private[this] val kafkaConsumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers(bootstrapAddress)
    .withGroupId("kafka-compaction-process-group-id")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    .withStopTimeout(Duration.Zero)


  def run(): Future[Any] = RunnableGraph.fromGraph {
    val consoleSink = Sink.fold[(TombstonesMessages, TotalMessages), UpdateResponse](0, 0) { case ((prevTombstones, prevTotal), res) =>
      val newTotal = prevTotal + 1
      if (res.isTombstone) {
        val newTombstones = prevTombstones + 1
        if(newTotal % 1_000_000 == 0) {
          logger.info(s"🗑 (Total Kafka Messages: $newTotal, Tombstones: $newTombstones, Messages Count: ${newTotal - newTombstones}, Distinct Keys: ${StorageService.getDistinctKeys}): Received TombstoneMessage with key: ${res.key}")
        }
        (newTombstones, newTotal)
      } else {
        if(newTotal % 1_000_000 == 0) {
          logger.info(s"🧮 (Total Kafka Messages: $newTotal, Tombstones: $prevTombstones, Messages Count: ${newTotal - prevTombstones}, Distinct Keys: ${StorageService.getDistinctKeys}): Received KafkaMessage with key: ${res.key}")
        }
        (prevTombstones, newTotal)
      }
    }

    // Creating the main RunnableGraph
    GraphDSL.create(consoleSink) { implicit builder =>
      sink =>
        import GraphDSL.Implicits._
        // Can be changed to plainPartitionedSource to optimize
        val source = builder.add(Consumer.plainSource(kafkaConsumerSettings, Subscriptions.topics(Set(topic))).idleTimeout(idleTimeout))
        val update = builder.add(Flow[ConsumerRecord[String, String]].map(StorageService.updateDistinctKeys))
        source ~> update ~> sink
        ClosedShape
    }
  }
    .run()
    .recover {
      case _: TimeoutException => Future.successful(()) // This is ugly but its fine for v1
    }
}

object ConsumerStream {
  type TombstonesMessages = Int
  type TotalMessages = Int
}
