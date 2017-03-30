package org.insightedge.examples.financialengineering.jobs

import kafka.serializer.{Decoder, StringDecoder}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.insightedge.examples.financialengineering.kafka.MarketTickDecoder
import org.insightedge.examples.financialengineering.model._
import org.insightedge.examples.financialengineering.{KafkaSettings, SparkUsage}
import org.insightedge.spark.implicits.all._
import org.insightedge.examples.financialengineering.SparkSettings._
import org.insightedge.examples.financialengineering.repos.TickerSymbols

/**
  * User: jason nerothin
  *
  * Time: 1:28 PM
  * Reads from Kafka, Writes to Data Grid.
  *
  * Each Ingest job tracks one TickerSymbol.
  * Each tickerSymbol stored in the Data Grid has a dedicated Kafka topic.
  * If a tickerSymbol does not have a pre-existing Kafka topic, creation of the KafkaProducer
  * will cause one to be created (if the Kafka server config is setup to support this).
  */
object Ingest extends SparkUsage{

  import org.apache.spark.streaming.kafka._

  val keyDecoder: Decoder[String] = new StringDecoder()
  val valueDecoder: Decoder[MarketTick] = new MarketTickDecoder

  val streamingCtx: StreamingContext = makeStreamingContext(ingestionAppName, ingestContextFrequencyMilliseconds)

  def main(args: Array[String]): Unit = {

    def createTickData(t: MarketTick): TickData = {
      TickData(id = null,
        symbol = t.id,
        timestampMs = t.timestamp,
        close = t.close,
        volume = t.volume,
        earnings = t.earnings,
        dividends = t.dividends,
        open = t.open,
        processed = false
      )
    }

    val tickerSymbol: Option[TickerSymbol]
        =  Some(TickerSymbol(KafkaSettings.testSymbol, -1, -1, -1, -1))
//      = TickerSymbols.provideTickerSymbolForIngestion()

    val tickerSymbolAbbreviation = tickerSymbol.get.abbreviation

    println(s"Processing Kafka Topic for symbol [$tickerSymbolAbbreviation]")

    tickerSymbol match {

      case Some(symbol) =>

        val topics = Set(tickerSymbolAbbreviation)

        val marketTicks: DStream[MarketTick] = KafkaUtils.createDirectStream[String, MarketTick, StringDecoder, MarketTickDecoder](
          streamingCtx,
          KafkaSettings.kafkaParams,
          topics
        ).map(_._2.asInstanceOf[MarketTick])

        marketTicks.print()

        val tickDataStream: DStream[TickData] = marketTicks.map[TickData](createTickData)

        tickDataStream.print()

        tickDataStream.saveToGrid()

        TickerSymbols.returnSymbolFromIngestion(symbol)

      case None =>

        println("There are already enough Ingest Threads")

    }

    streamingCtx.start()
    streamingCtx.awaitTerminationOrTimeout(120 * 1000) // 2 minutes
    streamingCtx.sparkContext.stopInsightEdgeContext()

  }

}