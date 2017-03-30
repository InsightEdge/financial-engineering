package org.insightedge.examples.financialengineering.jobs

import com.gigaspaces.client.ChangeSet
import com.gigaspaces.query.IdQuery
import org.apache.spark.streaming.StreamingContext
import org.insightedge.examples.financialengineering.model._
import org.insightedge.examples.financialengineering.{SpaceUsage, SparkUsage}
import org.insightedge.spark.implicits.all._
import org.insightedge.examples.financialengineering.SparkSettings._
import org.insightedge.examples.financialengineering.repos.TickerSymbols
/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/20/17
  * Time: 12:21 PM
  *
  * In order for any [[org.insightedge.examples.financialengineering.model.MarketReturn]] to
  * be calculated for some time t, we need to collect returns from all [[org.insightedge.examples.financialengineering.model.Stock]]s
  * reporting at time t.
  *
  * For simplification, we assume that all [[org.insightedge.examples.financialengineering.model.Stock]]
  * prices are present for every tick. If they are not, we log a message and skip.
  */
object CalcMarketReturns extends SpaceUsage with SparkUsage{

  private val streamingCtx: StreamingContext =
    makeStreamingContext(
      calcIndividualAppName,
      calcIndividualContextFrequencyMilliseconds
    )
  private val sc = streamingCtx.sparkContext
  private val space = makeClusteredProxy()
  private val zero = 0D
  private val activeSymbolCount = TickerSymbols.tickerSymbolCount

  def main(args: Array[String]): Unit = {

    val returns = sc.gridSql[InvestmentReturn]("WHERE processed = false").collect()

    val allReturns: Map[Long, Array[InvestmentReturn]] =
      returns.groupBy(_.timestampMs).filter {
        _._2.length == activeSymbolCount
      }

    val mid: PartialFunction[InvestmentReturn, String] = {
      case x => x.investmentId
    }

    val timestamps = allReturns.keySet
    for (ts <- timestamps) {
      val rets = allReturns.getOrElse(ts, Array[InvestmentReturn]())
      val n = activeSymbolCount
      // TODO these would be more efficient in a while loop
      val meanReturn = rets.foldLeft(zero)((a, i: InvestmentReturn) => a + i.getPercentageRateOfReturn) / activeSymbolCount
      val sumOfSquaredDifferences = rets.foldLeft(zero)((acc, i: InvestmentReturn) => {
        val diff = meanReturn - i.percentageRateOfReturn
        diff * diff
      })
      val stdDev = math.sqrt(sumOfSquaredDifferences / (n - 1))
      val sampleVariance = stdDev * stdDev
      rets.foreach(markProcessed)
      space.write(MarketReturn(id = null, timestampMs = ts, percentageRateOfReturn = meanReturn, variance = sampleVariance, processed = true))
    }

  }

  private def markProcessed(ir: InvestmentReturn): Unit = {
    space.asyncChange(
      new IdQuery(classOf[InvestmentReturn], ir.id),
      new ChangeSet().set("processed", true)
    )
  }

}