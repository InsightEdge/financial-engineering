package org.insightedge.examples.financialengineering

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 3/30/17
  * Time: 12:16 PM
  */
object KafkaSettings {

  val kafkaBrokers = "127.0.0.1:9092"

  val testSymbol = "UTX"

  private val bootstrapServersConfig = "bootstrap.servers"
  private val bootstrapServersValue = kafkaBrokers
  private val brokers = kafkaBrokers
  private val keySerializerConfig = "key.serializer"
  private val valueSerializerConfig = "value.serializer"
  private val serializerClassConfig = "serializer.class"
  private val brokerListConfig = "metadata.broker.list"

  val kafkaParams: Map[String, String] = Map[String, String](
    KafkaSettings.brokerListConfig -> KafkaSettings.kafkaBrokers,
    "auto.offset.reset" -> "smallest"
  )

  def kafkaProducerProperties(): java.util.Map[String, Object] = {
    val props = new java.util.HashMap[String, Object]()
    props.put(bootstrapServersConfig, bootstrapServersValue)
    props.put(keySerializerConfig,
      "org.apache.kafka.common.serialization.StringSerializer")
    props.put(valueSerializerConfig,
      classOf[org.insightedge.examples.financialengineering.kafka.MarketTickSerializer].getName)
    props
  }

  def kafkaConsumerProperties(): Map[String, String] = {
    Map[String, String](
      brokerListConfig -> s"$brokers",
      bootstrapServersConfig -> bootstrapServersValue,
      "request.required.acks" -> "1",
      serializerClassConfig -> "kafka.serializer.StringEncoder",
      "partitioner.class" -> "example.producer.SimplePartitioner"
    )
  }

}
