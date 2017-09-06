package com.huuuge.actors

import akka.actor.{Actor, Props}
import com.huuuge.models.{CreateConsumer, Exc, Message, StartConsuming}
import com.typesafe.scalalogging.Logger

class KafkaConsumersSupervisorActor extends Actor {

  val logger = Logger("ConsumersSupervisor")

  override def preStart(): Unit = {
    logger.info("ConsumersSupervisorActor Start")
  }

  override def postStop(): Unit = {
    logger.info("ConsumersSupervisorActor Stop")
  }

  override def receive: Receive = {
    case CreateConsumer(topic) =>
      logger.info(s"Creating consumer actor for topic $topic")
      startConsumer(topic)
      sender() ! Message("Consumer started", 200)
    case Exc(message, topic) =>
      logger.warn("Consumer actor failed. Recreating it...")
      startConsumer(topic)
  }

  def startConsumer(topic: String):Unit = {
    logger.info(s"Starting consumer for topic $topic")
    val kafkaConsumerActor = context.actorOf(Props[KafkaConsumerActor])
    kafkaConsumerActor ! StartConsuming(topic)
  }
}
