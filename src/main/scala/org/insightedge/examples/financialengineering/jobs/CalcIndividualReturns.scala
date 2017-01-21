package org.insightedge.examples.financialengineering.jobs

import org.apache.spark.SparkContext
import org.apache.spark.streaming.StreamingContext
import org.insightedge.examples.financialengineering.{Settings, SpaceUsage, makeStreamingContext}
import org.insightedge.examples.financialengineering.model._
import org.insightedge.spark.implicits.all._
import InvestmentHelp.Help
import TickDataHelp.Help
import com.gigaspaces.client.{ChangeSet, WriteModifiers}
import com.gigaspaces.query.IdQuery

import scala.collection.mutable

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/20/17
  * Time: 12:21 PM
  *
  * Turns [[TickData]] into [[InvestmentReturn]]s.
  */
class CalcIndividualReturns extends SpaceUsage {

  private val streamingCtx: StreamingContext =
    makeStreamingContext(
      Settings.calcIndividualAppName,
      Settings.calcIndividualContextFrequencyMilliseconds
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
          (monthAgoTick(td), td)
        ).collect().toIterator

        while (itr.hasNext) {
          val next = itr.next
          val orig = next._2
          val asInv = orig.toInvestment(next._1)
          returns ++ List(InvestmentReturn(null, asInv.endDateMs, asInv.id, asInv.CAGR()))
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

  private def monthAgoTick(newTick: TickData)(implicit tickerSymbol: TickerSymbol, sc: SparkContext): TickData = {
    val sym = tickerSymbol.abbreviation
    val newTime = newTick.timestampMs - Settings.msPerMonth
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

}