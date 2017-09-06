import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.util.{Failure, Success}

object KafkaProducerApp extends App {

  val logger = Logger("KafkaProducerApp")

  implicit val system = ActorSystem("kafka-producer")
  implicit val materializer = ActorMaterializer()

  implicit val executionContext = system.dispatcher

  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers("192.168.64.47:19092")

  val done = Source(List("polska", "niemcy", "polska", "usa", "polska", "niemcy", "rosja", "wlochy", "usa", "ukraina"))
    .map(elem =>
      new ProducerRecord[Array[Byte], String]("mmy", "key".getBytes(), elem)
    )
    .runWith(Producer.plainSink(producerSettings))

  done.onComplete {
    case Success(e) => logger.info(s"Successful")
    case Failure(e) => logger.error(s"Failed", e)
  }
}
