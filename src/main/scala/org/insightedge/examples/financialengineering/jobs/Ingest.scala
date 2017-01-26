package org.insightedge.examples.financialengineering.jobs

import kafka.serializer.{Decoder, StringDecoder}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.insightedge.examples.financialengineering.kafka.MarketTickDecoder
import org.insightedge.examples.financialengineering.model._
import org.insightedge.examples.financialengineering.{Settings, SpaceUsage, makeStreamingContext}
import org.insightedge.spark.implicits.all._
import org.openspaces.core.GigaSpace

/**
  * User: jason nerothin
  *
  * Time: 1:28 PM
  * Reads from Kafka, Writes to Data Grid.
  *
  * Each Injest job tracks one TickerSymbol.
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

    def createTickData(t:MarketTick) : TickData = {
      new TickData(id = null,
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

    val tickerSymbol: Option[TickerSymbol] = Some( new TickerSymbol("UTX", -1, -1, -1, -1) )

    val tickerSymbolAbbreviation = tickerSymbol.get.abbreviation;

    println(s"Processing Kafka Topic for symbol [$tickerSymbolAbbreviation]")

    tickerSymbol match {

      case Some(symbol) =>

        val topics = Set(tickerSymbolAbbreviation)
        val kafkaParams = Map[String, String](
          "metadata.broker.list" -> "localhost:9092",
          "auto.offset.reset" -> "smallest"
        )
        val marketTicks: DStream[MarketTick] = KafkaUtils.createDirectStream[String, MarketTick, StringDecoder, MarketTickDecoder](
          streamingCtx,
          kafkaParams,
          topics
        ).map(_._2.asInstanceOf[MarketTick])

        marketTicks.print()

        val tickDataStream: DStream[TickData] = marketTicks.map[TickData]( createTickData )

        tickDataStream.print()

        tickDataStream.saveToGrid()

        TickerSymbols.returnSymbolFromIngestion(symbol)

      case None =>

        println("There are already enough Ingest Threads")

    }

    streamingCtx.start()
    streamingCtx.awaitTerminationOrTimeout(120 * 1000) // 2 minutes
    streamingCtx.sparkContext.stopInsightEdgeContext()

//    TickerSymbols.provideTickerSymbolForIngestion() match {
//
//      case Some(tickerSymbol) =>
//
//        val ticker: DStream[(Nothing, Nothing)] = KafkaUtils.createDirectStream(
//          streamingCtx,
//          financialengineering.kafkaConsumerProperties(),
//          Set(tickerSymbol.abbreviation)
//        )
//
//        val tickData: DStream[TickData] = ticker.map[TickData] { packet =>
//          val tick = packet._2.asInstanceOf[MarketTick]
//          val td = TickData(id=null,
//            symbol=tickerSymbol.abbreviation,
//            timestampMs = tick.timestamp,
//            close = tick.close,
//            volume = tick.volume,
//            earnings = tick.earnings,
//            dividends = tick.dividends,
//            open = tick.open
//          )
////          println(s"Ingested tick data: $td")
//          td
//        }
//        tickData.foreachRDD(println(_))
//        tickData.saveToGrid()

//        val countDStream:DStream[Long] = tickData.count()
//        println("XXXXXXXXX: Number of tick data processed: ")
//        countDStream.


//      case None =>
//        println("There are already enough Ingest Threads")
//
//    }

  }

}