package com.huuuge.models

case class CreateConsumer(topic: String)
case class StartConsuming(topic: String)
case class Message(text: String, code: Long)
case class Response(text: String, code: Long)
case class Exc(cause: String, topic: String)
