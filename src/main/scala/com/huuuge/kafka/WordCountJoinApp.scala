package com.huuuge.kafka

import java.lang.{Long => JLong}
import java.util.Properties
import java.util.concurrent.CountDownLatch

import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.{KStream, KStreamBuilder, KTable}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

object WordCountJoinApp extends App {
  val logger = Logger("WordCountJoinApp")

  logger.info("Start word count querying...")

  val props = new Properties
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-wordcount-join")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.64.47:19092")
  props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
  props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
  props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10000.asInstanceOf[Object])
  props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val stringSerde: Serde[String] = Serdes.String()
  val longSerde: Serde[JLong] = Serdes.Long()

  val builder = new KStreamBuilder
  val wordsStream: KStream[String, String] = builder.stream("rgr")
  val wordsCountTable: KTable[String, String] = builder.table("rwo")

  val clicksPerRegion: KStream[String, String] =
    wordsStream.leftJoin(wordsCountTable, (word: String, count: String) => count)

  clicksPerRegion.to("mpr")

  val streams = new KafkaStreams(builder, props)
  val latch = new CountDownLatch(1)

  // attach shutdown handler to catch control-c
  Runtime.getRuntime.addShutdownHook(new Thread("streams-wordcount-join-shutdown-hook") {
    override def run(): Unit = {
      streams.close()
      latch.countDown()
      logger.warn(s"Stream has been closed ${latch.getCount}")
    }
  })

  try {
    streams.start()
    logger.info("Stream has been started")
    latch.await()
  } catch {
    case e: Throwable =>
      System.exit(1)
  }
}
