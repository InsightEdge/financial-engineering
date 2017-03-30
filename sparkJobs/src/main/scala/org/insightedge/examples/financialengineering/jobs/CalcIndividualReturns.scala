package org.insightedge.examples.financialengineering.jobs

import java.time.Duration

import org.apache.spark.SparkContext
import org.apache.spark.streaming.StreamingContext
import org.insightedge.examples.financialengineering._
import org.insightedge.examples.financialengineering.model._
import org.insightedge.spark.implicits.all._
import InvestmentHelp._
import TickDataHelp._
import com.gigaspaces.client.{ChangeSet, WriteModifiers}
import com.gigaspaces.query.IdQuery

import scala.collection.mutable
import SparkSettings._
import org.insightedge.examples.financialengineering.repos.TickerSymbols

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/20/17
  * Time: 12:21 PM
  *
  * Turns [[TickData]] into [[InvestmentReturn]]s.
  */
object CalcIndividualReturns extends SpaceUsage with SparkUsage {

  private val streamingCtx: StreamingContext =
    makeStreamingContext(
      calcIndividualAppName,
      calcIndividualContextFrequencyMilliseconds
    )
  private val sc = streamingCtx.sparkContext
  private val space = makeClusteredProxy()

  def main(args: Array[String]): Unit = {

    TickerSymbols.provideTickerSymbolForCalcIndividualReturn() match {

      case Some(tickerSymbol) =>

        val sym = tickerSymbol.abbreviation

        val tdRdd = sc.gridSql[TickData](s"WHERE symbol = '$sym' AND processed = false")

        val returns = mutable.LinkedList[InvestmentReturn]()
        val ticksToRetire = mutable.LinkedList[TickData]()

        val itr = tdRdd.map(td =>
          (monthAgoTick(td, tickerSymbol, sc), td)
        ).collect().toIterator

        while (itr.hasNext) {
          val next = itr.next
          val orig = next._2
          //          val asInv = orig.toInvestment(next._1) For some reason our implicit classes make the compiler angry
          val asInv = toInvestment(orig, next._1)
          //          returns ++ List(InvestmentReturn(null, asInv.endDateMs, asInv.id, asInv.CAGR()))
          val cagr = compoundAnnualGrowthRate(asInv)
          returns ++ List(InvestmentReturn(null, asInv.endDateMs, asInv.id, cagr))
          retire(orig)
        }
        space.writeMultiple(ticksToRetire.toArray, WriteModifiers.UPDATE_ONLY)

        streamingCtx.sparkContext.parallelize(returns).saveToGrid()

        TickerSymbols.returnSymbolFromCalcIndividualReturn(tickerSymbol)

        streamingCtx.start()
        streamingCtx.awaitTerminationOrTimeout(100)
        streamingCtx.sparkContext.stopInsightEdgeContext()

      case _ => println("There are already enough CalcIndividualReturn Threads.")

    }

  }

  private val msIn5Minutes = 60 * 1000 * 5

  private def monthAgoTick(newTick: TickData, tickerSymbol: TickerSymbol, sc: SparkContext): TickData = {
    val sym = tickerSymbol.abbreviation
    val newTime = newTick.timestampMs - CoreSettings.msPerMonth
    val oldTime = newTime - msIn5Minutes
    val oldTick = sc.gridSql[TickData](s"WHERE symbol = '$sym' " +
      s"AND processed = true " +
      s"AND timestampMs <= $newTime " +
      s"AND timestampMs >= $oldTime" +
      "ORDER BY timestampMs DESC")
    oldTick.first
  }

  private def retire(tick: TickData): Unit = {
    space.asyncChange(new IdQuery(tick.getClass, tick.id), new ChangeSet().set("processed", true))
  }

  /**
    * Copied from [[InvestmentHelp]]
    */
  def duration(i: Investment): Duration = {
    // 1440000 / 518400000
    Duration.ofMillis(i.endDateMs - i.startDateMs)
  }

  /**
    * Copied from [[InvestmentHelp]]
    */
  def years(i: Investment): Double = {
    (i.endDateMs - i.startDateMs) / (CoreSettings.daysPerYear * CoreSettings.msPerDay.toDouble)
  }

  /**
    * Copied from [[InvestmentHelp]]
    */
  def compoundAnnualGrowthRate(i: Investment): Double = {
    math.pow((i.sellPrice + i.dividendsDuringTerm - i.buyCost - i.sellCost) / i.buyPrice, 1 / years(i)) - 1
  }


  /**
    * Copied from [[TickDataHelp]]
    */
  def toInvestment(d: TickData, d2: TickData): Investment = {
    var first: TickData = null
    var second: TickData = null
    if (isEarlierThan(d2, d)) {
      first = d2
      second = d
    } else {
      first = d
      second = d2
    }
    Investment(null,
      buyPrice = first.close,
      sellPrice = second.open,
      startDateMs = first.timestampMs,
      endDateMs = second.timestampMs
    )
  }

  /**
    * Copied from [[TickDataHelp]]
    */
  def isEarlierThan(d: TickData, d2: TickData): Boolean = {
    d2.timestampMs > d.timestampMs
  }

}