package org.insightedge.examples.financialengineering.jobs

import org.apache.spark.SparkContext
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.insightedge.examples.financialengineering
import org.insightedge.examples.financialengineering.{Settings, SpaceUsage, makeSparkConf, makeStreamingContext}
import org.insightedge.examples.financialengineering.{Settings, makeSparkConf, makeStreamingContext}
import org.insightedge.examples.financialengineering.model._
import org.insightedge.spark.implicits.all._

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/20/17
  * Time: 12:21 PM
  */
class CalcIndividualReturn {

  val streamingCtx: StreamingContext = makeStreamingContext(Settings.calcIndividualAppName, Settings.calcIndividualContextFrequencyMilliseconds)

  private def retrieveStock(tickerSymbol: TickerSymbol) = {
    val sym = tickerSymbol.abbreviation
    val STOCKS = streamingCtx.sparkContext.gridSql[Stock](s"WHERE tickerSymbol = $sym").toLocalIterator.toList
    require(STOCKS.nonEmpty, s"No Stock found for symbol: $sym")
    val stock = STOCKS.head
    stock
  }

  def main(args: Array[String]): Unit = {

    // TODO finish!

    TickerSymbols.provideTickerSymbolForCalcIndividualReturn() match {

      case Some(tickerSymbol) =>

        val ticker: DStream[(Nothing, Nothing)] = KafkaUtils.createDirectStream(
          streamingCtx,
          financialengineering.kafkaConsumerProperties(),
          Set(tickerSymbol.abbreviation)
        )

        val tickData: DStream[TickData] = ticker.map[TickData] { packet =>
          val tick = packet._2.asInstanceOf[MarketTick]
          val td = TickData(id = null,
            symbol = tickerSymbol.abbreviation,
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
        tickData.saveToGrid()

        val stock: Stock = retrieveStock(tickerSymbol)

        TickerSymbols.returnSymbolFromCalcIndividualReturn(tickerSymbol)

      case _ =>
        println("There are already enough CalcMarketReturn Threads.")

    }

  }



}
