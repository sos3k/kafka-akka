package com.huuuge.kafka

import java.lang.{Long => JLong}
import java.util.Properties
import java.util.concurrent.CountDownLatch

import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

object WordCountApp extends App {

  val logger = Logger("WordCountApp")

  logger.info("Start word counting...")

  val props = new Properties
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-wordcount")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.64.47:19092")
  props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
  props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
//  props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10000.asInstanceOf[Object])
  props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val stringSerde: Serde[String] = Serdes.String()
  val longSerde: Serde[JLong] = Serdes.Long()

  val builder = new KStreamBuilder
  val source: KStream[String, String] = builder.stream("mmy")

  import com.huuuge.kafka.implicits.KeyValueImplicits._

  import collection.JavaConverters.asJavaIterableConverter

  val ss: KStream[String, String] = source.flatMapValues(value => value.toLowerCase.split("\\W+").toIterable.asJava)
  val sss: KStream[String, String] = ss.map((key, value) => {
    logger.info(s"Key $key Value $value")
    (value, value)
  })
  val s: KGroupedStream[String, String] = sss.groupByKey()
  val c: KTable[String, String] = s.count("count-store-long").mapValues(value => value.toString)

  c.to(stringSerde, stringSerde, "rwo")
  sss.to("rgr")

  val streams = new KafkaStreams(builder, props)
  val latch = new CountDownLatch(1)

  // attach shutdown handler to catch control-c
  Runtime.getRuntime.addShutdownHook(new Thread("streams-wordcount-shutdown-hook") {
    override def run(): Unit = {
      streams.close()
      latch.countDown()
      logger.warn(s"Stream has been closed ${latch.getCount}")
    }
  })

  try {
    streams.start()
    logger.info("Stream has been started")


    streams.setStateListener((newState, oldState) => {
      logger.info(s"Old state $oldState New state $newState")
      if (newState.name().equals("RUNNING")) {
        val queryableStoreName: String = c.queryableStoreName()
        if (queryableStoreName != null) {
          val view = streams.store(queryableStoreName, QueryableStoreTypes.keyValueStore[String, Long]())
          val v = view.get("polska")
          logger.info(s"Value $v")
        } else {
          logger.warn("Table not querable")
        }
      }
    })

    latch.await()
  } catch {
    case e: Throwable =>
      System.exit(1)
  }
}
