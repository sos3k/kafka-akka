package com.huuuge.actors

import akka.Done
import akka.actor.{Actor, ActorRef}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.huuuge.models.{Exc, StartConsuming}
import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.{LongDeserializer, StringDeserializer}

import scala.concurrent.Future
import scala.util.Failure

class KafkaConsumerActor extends Actor {

  val logger = Logger("KafkaConsumerActor")

  import context.dispatcher
  implicit val materializer = ActorMaterializer()

  val consumerSettings: ConsumerSettings[String, String] = ConsumerSettings(context.system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers("192.168.64.47:19092")
    .withGroupId("group1")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")

  override def preStart(): Unit = {
    logger.info("KafkaConsumerActor Start")
  }

  override def postStop(): Unit = {
    logger.info("KafkaConsumerActor Stop")
  }

  override def receive: Receive = {
    case StartConsuming(topic) =>
      logger.info(s"Started consumer for topic $topic")
      val done =
        Consumer.committableSource(consumerSettings, Subscriptions.topics(topic))
          .mapAsync(1) {
            msg => {
              logger.info(getPrintableMessage(topic, msg.record))
              Future.successful(Done)
            }.map(_ => msg)
          }
          .mapAsync(1) { msg =>
            msg.committableOffset.commitScaladsl()
          }
          .runWith(Sink.ignore)

      done.onComplete({
        case Failure(cause) => sender() ! Exc(cause.getMessage, topic)
        case _ => logger.info("Done successfully")
      })
  }

  def getPrintableMessage(topic: String, record: ConsumerRecord[String, String]) =
      s"[${topic.toUpperCase}] Key ${record.key} Value ${record.value} Actor $self"
}
