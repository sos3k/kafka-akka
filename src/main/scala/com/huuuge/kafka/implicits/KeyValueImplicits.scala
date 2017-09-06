package com.huuuge.kafka.implicits

import org.apache.kafka.streams.KeyValue

object KeyValueImplicits {

    implicit def Tuple2ToKeyValue[K, V](tuple: (K, V)): KeyValue[K, V] = new KeyValue(tuple._1, tuple._2)
}
