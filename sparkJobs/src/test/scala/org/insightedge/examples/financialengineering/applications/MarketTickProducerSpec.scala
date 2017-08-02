package org.insightedge.examples.financialengineering.applications

import org.scalatest._
import net.manub.embeddedkafka._

import org.insightedge.examples.financialengineering.kafka.{MarketTickDecoder, MarketTickSerializer}
import org.insightedge.examples.financialengineering.model._

import org.apache.kafka.clients.producer.{Producer, KafkaProducer, ProducerRecord}

import net.manub.embeddedkafka.ConsumerExtensions._
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.StringDeserializer
import scala.collection.JavaConverters._
import org.apache.kafka.common.TopicPartition
import org.insightedge.examples.financialengineering.KafkaSettings

class MarketTickProducerSpec extends WordSpec with EmbeddedKafka with Consumers with Matchers {
  "'AAPL' market tick records" should {
    "be written to Kafka" in {
        
      withRunningKafka {
        
        val customBrokerConfig = Map("auto.offset.reset" -> "smallest", "auto.create.topics.enable" -> "true", "num.partitions" -> "1")
        implicit val customKafkaConfig = EmbeddedKafkaConfig(customBrokerProperties = customBrokerConfig)
      
        val decoder = new MarketTickDecoder()
        implicit val keyDeserializer = new StringDeserializer()
        implicit val valueDeserializer = MarketTickDeserializer(decoder)
      
        // now a kafka broker is listening on port 12345
        val producer: Producer[String, MarketTick] = aKafkaProducer thatSerializesValuesWith classOf[MarketTickSerializer]
        
        val testSym = new TickerSymbol("AAPL")
        
        val path = getClass.getResource("/").getPath
        val marketTickProducer = new MarketTickProducer(path, testSym, producer)
        marketTickProducer.run()
         
        val consumer = withConsumer[String, MarketTick, Unit] { consumer =>
          consumer.subscribe(Set(KafkaSettings.kafkaTopic).asJava)
          val res = consumer.poll(5000).records(new TopicPartition(KafkaSettings.kafkaTopic, 0)).asScala.map(cr => cr.value())

          //'allstocks_19980106' file
          val mt1 = MarketTick(884095680000l, "1998-01-06", 0.5166, 0.5166, 0.5166, 0.5166, 61481.0, 28.0, 0.0, 0.0)
          val mt2 = MarketTick(884096640000l, "1998-01-06", 0.5205, 0.5205, 0.5205, 0.5205, 30740.5, 28.0, 0.0, 0.0)
          //'allstocks_19980107' file
          val mt3 = MarketTick(884178840000l, "1998-01-07", 0.6142, 0.6142, 0.6142, 0.6142, 107592.0, 28.0, 0.0, 0.0)
          val mt4 = MarketTick(884179560000l, "1998-01-07", 0.6142, 0.6142, 0.6142, 0.6142, 30740.5, 28.0, 0.0, 0.0)
          
          res.size should be (4)
          res should contain theSameElementsInOrderAs List(mt1, mt2, mt3, mt4)
        }
      }
    }
  }
}
case class MarketTickDeserializer(decoder: MarketTickDecoder) extends Deserializer[MarketTick] {
  
    override def configure(paramMap: java.util.Map[String, _], isKey: Boolean) = {}
  
    override def deserialize(topic: String, value: Array[Byte]): MarketTick = { decoder.fromBytes(value)}
  
    override def close() = {}
}