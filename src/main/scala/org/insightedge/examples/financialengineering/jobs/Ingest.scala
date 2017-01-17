package org.insightedge.examples.financialengineering.jobs

import kafka.serializer.{Decoder, StringDecoder}
import org.apache.spark.streaming.StreamingContext
import org.insightedge.examples.financialengineering
import org.insightedge.examples.financialengineering.{Settings, SpaceUsage, makeStreamingContext}
import org.insightedge.examples.financialengineering.model._
import org.openspaces.core.GigaSpace
import org.insightedge.spark.implicits.all._
import org.apache.spark.streaming.dstream.DStream
import org.insightedge.examples.financialengineering.kafka.MarketTickDecoder

/**
  * User: jason nerothin
  *
  * Time: 1:28 PM
  * Reads from Kafka, Writes to Data Grid.
  *
  * Each Inject job tracks one TickerSymbol.
  * Each tickerSymbol stored in the Data Grid has a dedicated Kafka topic.
  * If a tickerSymbol does not have a pre-existing Kafka topic, creation of the KafkaProducer
  * will cause one to be created (if the Kafka server config is setup to support this).
  */
object Ingest extends SpaceUsage {

  import org.apache.spark.streaming.kafka._

  val keyDecoder: Decoder[String] = new StringDecoder()
  val valueDecoder: Decoder[MarketTick] = new MarketTickDecoder

  val streamingCtx: StreamingContext = makeStreamingContext(Settings.ingestionAppName, Settings.ingestContextFrequencyMilliseconds)
  val space: GigaSpace = makeClusteredProxy()

  def main(args: Array[String]): Unit = {

    TickerSymbols.provideTickerSymbolForIngestion() match {

      case Some(tickerSymbol) =>

        val ticker: DStream[(Nothing, Nothing)] = KafkaUtils.createDirectStream(
          streamingCtx,
          financialengineering.kafkaConsumerProperties(),
          Set(tickerSymbol.abbreviation)
        )

        val tickData: DStream[TickData] = ticker.map[TickData] { packet =>
          val tick = packet._2.asInstanceOf[MarketTick]
          val td = TickData(id=null,
            symbol=tickerSymbol.abbreviation,
            timestampMs = tick.timestamp,
            close = tick.close,
            volume = tick.volume,
            earnings = tick.earnings,
            dividends = tick.dividends,
            open = tick.open
          )
          println(s"Ingested tick data: $td")
          td
        }
        tickData.foreachRDD(println(_))
        tickData.saveToGrid()

//        val countDStream:DStream[Long] = tickData.count()
//        println("XXXXXXXXX: Number of tick data processed: ")
//        countDStream.

        TickerSymbols.returnSymbolFromIngestion(tickerSymbol)

        streamingCtx.start()
        streamingCtx.awaitTerminationOrTimeout(100)
        streamingCtx.sparkContext.stopInsightEdgeContext()

      case None =>
        println("There are already enough Ingest Threads")

    }

  }

}