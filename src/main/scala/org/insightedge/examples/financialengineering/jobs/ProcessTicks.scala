/*
 * Copyright (c) 2016, GigaSpaces Technologies, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.insightedge.examples.financialengineering.jobs

import org.apache.spark.SparkContext
import org.insightedge.examples.financialengineering.{Settings, SpaceUsage, makeSparkConf, makeStreamingContext}
import org.insightedge.examples.financialengineering.finance.SecurityCharacteristics._
import org.insightedge.examples.financialengineering.model._
import org.insightedge.spark.implicits.all._

import scala.collection.mutable

/** @author Jason Nerothin
  */
object ProcessTicks extends SpaceUsage {

  //  val streamingCtx:StreamingContext = makeStreamingContext(Settings.processorAppName, Settings.tickProcessorFrequencyMilliseconds)

  val sc: SparkContext = new SparkContext(makeSparkConf(Settings.processorAppName))

  /**
    * The MarketReturn, StockReturn, TBill, TickData, Trade datatypes all carry a timestamp.
    * This setting determines the time resolution of the data links: If it's higher,
    */
  private val timeDifferenceToleranceMs = 1000

  def zipEverything(timesTradesReturnsAndTBills: IndexedSeq[(Long, Trade, Double, TBill)],
                    timesAndMarketReturns: mutable.Map[Long, mutable.LinkedList[Double]]):
  // timesTradesStocksReturnsTBillsAndMarketReturns
  IndexedSeq[(Long, Trade, AssetReturn, TBill, Double, MarketReturn)] = {
    timesTradesReturnsAndTBills
    null
  }

  def doRegression(timesTradesStocksReturnsTBillsReturnsAndMarketReturns: IndexedSeq[(Long, Trade, AssetReturn, TBill, Double, MarketReturn)]) = {

  }

  def main(args: Array[String]) {

    TickerSymbols.provideTickerSymbolForTickProcessor() match {

      case Some(tickerSymbol) =>

        val stock: Stock = retrieveStock(tickerSymbol)

        // import tick data
        val timeLimit = Settings.ticksPerMonth * 2
        val tickRdd =
          sc.gridSql[TickData](s"WHERE symbol = $tickerSymbol ORDER BY timestampMs DESC LIMIT $timeLimit")
        tickRdd.cache()
        val ticks = tickRdd.toLocalIterator.toList

        // time cutoffs
        val oneMonthBeforeFirstTrade = ticks.last.timestampMs - Settings.msPerMonth
        val bondDateCutoff = oneMonthBeforeFirstTrade - Settings.msPerDay
        val marketReturnDateCutoff = bondDateCutoff

        // times come from Ticks
        // NOTE: Ticks reuse Trade as a representation of the value
        // change in a Stock from the time of the Tick to the time of
        // a month previous.
        val timesTradesAndReturns: IndexedSeq[(Long, Trade, Double)] =
          computeTimesTradesAndReturns(ticks, oneMonthBeforeFirstTrade)

        /** This time match is different from the others in that it
          * doesn't pay attention to [[timeDifferenceToleranceMs]].
          *
          * The returned time comes from Trades.
          */
        val timesTradesReturnsAndTBills: IndexedSeq[(Long, Trade, Double, TBill)] =
          zipByTimeWithRetrievedTBills(timesTradesAndReturns, bondDateCutoff)

        val stocksAndTrades: List[(Stock, Trade)] =
          timesTradesReturnsAndTBills.map { i => (stock, i._2) }.toList
        val stocksTradesAndReturns: List[((Stock, Trade), Double)] =
          stocksAndTrades zip weightedMonthlyReturns(stocksAndTrades)

        // times come from MarketReturns
        val timesAndMarketReturns: mutable.Map[Long, mutable.LinkedList[Double]] =
          computeMarketReturns(stocksTradesAndReturns, marketReturnDateCutoff)

        // times come from Trade (which comes from TickData)
        val timesTradesStocksReturnsTBillsReturnsAndMarketReturns:
          IndexedSeq[(Long, Trade, AssetReturn, TBill, Double, MarketReturn)] =
          zipEverything(timesTradesReturnsAndTBills, timesAndMarketReturns)

        val vectors = doRegression(timesTradesStocksReturnsTBillsReturnsAndMarketReturns)

        // TODO save result

        TickerSymbols.returnSymbolFromTickProcessor(tickerSymbol)

      case None => println("All ticker symbols have been allocated enough ProcessTicks Threads.")

    }

  }

  // access reference data
  // REST
  // Polling
  // Spark + SOR, no more ETL bottleneck - unified data model
  // Use existing data model...
  // Ali's deck - forester - decade+ experience
  // A Mongo replacement Space

  private def computeMarketReturns(stocksTradesAndReturns: List[((Stock, Trade), Double)],
                                   marketReturnDateCutoff: Long)
  : mutable.Map[Long, mutable.LinkedList[Double]] = {

    val currentReturns: List[(Long, Double)] = stocksTradesAndReturns.map {
      _._1._2
    }.map { tr =>
      (tr.endDateMs, monthlyRateOfReturn(tr))
    }

    val timestamps = currentReturns.map(_._1).mkString(",")

    val existingStockReturns: List[(Long, Double)] = sc.gridSql[AssetReturn](s"WHERE timestampMs IN ($timestamps)")
      .map { sr =>
        (sr.timestampMs, sr.percentageRateOfReturn)
      }.toLocalIterator.toList

    val timeToReturns = mutable.HashMap[Long, mutable.LinkedList[Double]]()

    for (existingStockReturn <- existingStockReturns) {
      val timestamp = existingStockReturn._1
      if (timestamp > marketReturnDateCutoff) {
        val list = timeToReturns.get(timestamp) match {
          case Some(x: mutable.LinkedList[Double]) =>
            x ++ mutable.LinkedList(existingStockReturn._2)
          case _ =>
            mutable.LinkedList()
        }
        timeToReturns(timestamp) = list.asInstanceOf[mutable.LinkedList[Double]]
      }
    }

    timeToReturns

  }

  private def zipByTimeWithRetrievedTBills(timesTradesAndReturns: IndexedSeq[(Long, Trade, Double)], bondDateCutoff: Long) = {
    val tBillItr = sc.gridSql[TBill](s"WHERE timestampMs >= $bondDateCutoff").toLocalIterator
    require(tBillItr.nonEmpty, "No TBills available to use as zero-risk securities.")
    var currTBill = tBillItr.next
    val timesTradesReturnsAndTBills: IndexedSeq[(Long, Trade, Double, TBill)] = timesTradesAndReturns.map {
      tar =>
        val tradeTime = tar._1
        if (tradeTime < currTBill.timestampMs && tBillItr.hasNext) {
          val tempTBill = currTBill
          currTBill = tBillItr.next
          (tradeTime, tar._2, tar._3, tempTBill)
        }
    }.asInstanceOf[IndexedSeq[(Long, Trade, Double, TBill)]]
    timesTradesReturnsAndTBills
  }

  def computeTimesTradesAndReturns(ticks: List[TickData], cutoff: Long): IndexedSeq[(Long, Trade, Double)] = {
    val len = ticks.length
    println(s"Processing $len MarketTicks.")
    val halfway = len / 2

    (1 until halfway)
      .map {
        i: Int => (i, i + halfway)
      }
      .map {
        tup =>
          val recent = ticks(tup._2)
          val historic = ticks(tup._1)
          val trade = Trade(
            id = null,
            buyPrice = historic.open,
            sellPrice = recent.close,
            buyDateMs = historic.timestampMs,
            endDateMs = recent.timestampMs
          )
          (recent.timestampMs, trade, monthlyRateOfReturn(trade))
      }
      .filter(_._1 > cutoff) // for cases where there is insufficient back-data - e.g. t=[0,2 mo)
    // i.e before the 2nd full month of data
  }

  private def retrieveStock(tickerSymbol: TickerSymbol) = {
    val sym = tickerSymbol.abbreviation
    val STOCKS = sc.gridSql[Stock](s"WHERE tickerSymbol = $sym").toLocalIterator.toList
    require(STOCKS.nonEmpty, s"No Stock found for symbol: $sym")
    val stock = STOCKS.head
    stock
  }

}