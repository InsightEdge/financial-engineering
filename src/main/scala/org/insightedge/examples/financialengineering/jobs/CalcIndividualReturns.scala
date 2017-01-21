package org.insightedge.examples.financialengineering.jobs

import org.apache.spark.SparkContext
import org.apache.spark.streaming.StreamingContext
import org.insightedge.examples.financialengineering.{Settings, SpaceUsage, makeStreamingContext}
import org.insightedge.examples.financialengineering.model._
import org.insightedge.spark.implicits.all._
import InvestmentHelp.Help
import TickDataHelp.Help
import org.openspaces.core.GigaSpace

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

  val streamingCtx: StreamingContext =
    makeStreamingContext(
      Settings.calcIndividualAppName,
      Settings.calcIndividualContextFrequencyMilliseconds
    )

  val space: GigaSpace = makeClusteredProxy()

  def main(args: Array[String]): Unit = {

    TickerSymbols.provideTickerSymbolForCalcIndividualReturn() match {

      case Some(tickerSymbol) =>
        val sym = tickerSymbol.abbreviation
        val sc = streamingCtx.sparkContext

        val tdRdd = sc.gridSql[TickData](s"WHERE symbol = '$sym' AND processed = false")

        val itr = tdRdd.map(td =>
          (monthAgoTick(td), td)
        ).collect().toIterator

        val returns: mutable.LinkedList[InvestmentReturn] = mutable.LinkedList[InvestmentReturn]()
        while (itr.hasNext) {
          val n = itr.next
          val i = n._1.toInvestment(n._2)
          returns ++ List(InvestmentReturn(null, i.endDateMs, i.id, i.CAGR()))
        }
        space.writeMultiple(returns.toArray)

        TickerSymbols.returnSymbolFromCalcIndividualReturn(tickerSymbol)

        streamingCtx.start()
        streamingCtx.awaitTerminationOrTimeout(100)
        streamingCtx.sparkContext.stopInsightEdgeContext()

      case _ => println("There are already enough CalcMarketReturn Threads.")

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


}
