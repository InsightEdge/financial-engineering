package org.insightedge.examples.financialengineering
import collection.JavaConverters._
/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 3/30/17
  * Time: 12:16 PM
  */
object KafkaSettings {
  
  private val bootstrapServers = "localhost:9092"
  private val bootstrapServersKey = "bootstrap.servers"

  def kafkaParams() = Map[String, String](
     bootstrapServersKey -> bootstrapServers,
     "auto.offset.reset" -> "smallest"
  )

  def kafkaProducerProperties(): java.util.Map[String, Object] = Map[String, Object](
    bootstrapServersKey -> bootstrapServers,
    "request.required.acks" -> "0",
    "producer.type" -> "sync",
    "key.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
    "value.serializer" -> classOf[org.insightedge.examples.financialengineering.kafka.MarketTickSerializer].getName
  ).asJava
}
