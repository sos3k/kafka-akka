name := "kafka-akka"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies ++= {
  val KafkaStreamsVersion = "0.11.0.0"
  val AkkaKafkaVersion = "0.17"
  val ScalaLoggingVersion = "3.7.1"
  val LogbackVersion = "1.2.3"

  Seq(
    "org.apache.kafka" % "kafka-streams" % KafkaStreamsVersion,
    "com.typesafe.akka" %% "akka-stream-kafka" % AkkaKafkaVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % ScalaLoggingVersion,
    "ch.qos.logback" % "logback-classic" % LogbackVersion
  )
}

