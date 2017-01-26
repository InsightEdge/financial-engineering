package org.insightedge.examples

import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.insightedge.spark.context.InsightEdgeConfig
import org.insightedge.spark.implicits.all._


/**
  *
  * User: jason
  *
  * Time: 1:35 PM
  */
package object financialengineering{

  def makeStreamingContext(appName: String, frequencyInMilliseconds: Int): StreamingContext = {
    val sparkConf: SparkConf = makeSparkConf(appName, Settings.spaceName, Settings.spaceLookupGroups, Settings.spaceLookupLocators, Settings.sparkMasterUrl)
    new StreamingContext(sparkConf, Milliseconds(frequencyInMilliseconds))
  }

  def makeSqlContext(spaceName: String, spaceGroups: String, spaceLocators: String, sparkMasterUrl: String, appName: String): SQLContext = {
    val sparkConf = makeSparkConf(spaceName, spaceGroups, spaceLocators, sparkMasterUrl)
    val sc = new SparkContext(sparkConf)
    new SQLContext(sc)
  }

  def makeSparkConf(appName: String, spaceName: String = Settings.spaceName, spaceGroups: String = Settings.spaceLookupGroups, spaceLocators: String = Settings.spaceLookupLocators, sparkMasterUrl: String = Settings.sparkMasterUrl): SparkConf = {
    val ieConfig = InsightEdgeConfig(spaceName, Some(spaceGroups), Some(spaceLocators))
    new SparkConf().setAppName(appName).setMaster(sparkMasterUrl).setInsightEdgeConfig(ieConfig)
  }

  def kafkaConsumerProperties(): Map[String, String] = {
    Map[String,String](
      brokerListConfig -> s"$brokers",
      bootstrapServersConfig -> bootstrapServersValue,
      "request.required.acks" -> "1",
      serializerClassConfig -> "kafka.serializer.StringEncoder",
      "partitioner.class" -> "example.producer.SimplePartitioner"
    )
  }

  private val bootstrapServersConfig = "bootstrap.servers"
  private val bootstrapServersValue = Settings.kafkaBrokers

  private val serializerClassConfig = "serializer.class"

  private val brokerListConfig = "metadata.broker.list"
  private val brokers = Settings.kafkaBrokers

  private val keySerializerConfig = "key.serializer"
  private val valueSerializerConfig = "value.serializer"

  def kafkaProducerProperties(): java.util.Map[String, Object] = {
    val props = new java.util.HashMap[String,Object]()
    props.put(bootstrapServersConfig,bootstrapServersValue)
    props.put(keySerializerConfig,
      "org.apache.kafka.common.serialization.StringSerializer")
    props.put(valueSerializerConfig,
      "org.insightedge.examples.financialengineering.kafka.MarketTickSerializer")
    props
  }

}