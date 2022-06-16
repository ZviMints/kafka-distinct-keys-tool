import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import services.StorageService
import stream.ConsumerStream

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, DurationInt}

object Main extends App with StrictLogging {
    val bootstrapHost = ConfigFactory.load().getString("kafka.host")
    val kafkaTopics = ConfigFactory.load().getString("kafka.topic")
    logger.info(s"Starting with bootstrapHost: $bootstrapHost, kafkaTopics: $kafkaTopics ...")

   implicit val system: ActorSystem = ActorSystem()
   val stream = new ConsumerStream(s"${bootstrapHost}:9092", 30.seconds, kafkaTopics)
   Await.result(stream.run(), Duration.Inf)
   logger.info(s"📈 Total Distinct Keys: ${StorageService.getDistinctKeys}")
   system.terminate
    sys.exit()
}


