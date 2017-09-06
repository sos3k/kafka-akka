package com.huuuge

import akka.actor.{ActorSystem, Props}
import com.huuuge.actors.KafkaConsumersSupervisorActor
import com.huuuge.models.CreateConsumer
import com.typesafe.scalalogging.Logger

object ConsumerApp extends App {

  val logger = Logger("ConsumerApp")

  implicit val system = ActorSystem("kafka-consumer-system")
  implicit val executionContext = system.dispatcher

  startActors()

  def startActors(): Unit = {
    logger.info("Starting actors...")
    val consumersSupervisor = system.actorOf(Props[KafkaConsumersSupervisorActor])
    consumersSupervisor ! CreateConsumer("rwo")
    consumersSupervisor ! CreateConsumer("mpr")
    consumersSupervisor ! CreateConsumer("mmy")
    consumersSupervisor ! CreateConsumer("rgr")
  }
}
