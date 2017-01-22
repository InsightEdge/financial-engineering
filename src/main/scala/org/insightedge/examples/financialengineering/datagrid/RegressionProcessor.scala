package org.insightedge.examples.financialengineering.datagrid

import javax.annotation.Resource

import com.j_spaces.core.client.SQLQuery
import org.insightedge.examples.financialengineering.Settings
import org.insightedge.examples.financialengineering.finance.SimpleRegressionModel
import org.insightedge.examples.financialengineering.model.{CharacteristicLine, InvestmentReturn, MarketReturn}
import org.openspaces.core.GigaSpace
import org.openspaces.events.adapter.SpaceDataEvent
import org.openspaces.events.{EventDriven, EventTemplate}
import org.openspaces.events.polling.Polling

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/20/17
  * Time: 12:23 PM
  * When Spark saves a MarketReturn to the Data Grid, this
  * Polling Container notices it, and calculates a Security
  * Characteristic Line for all IndividualReturns that
  * arrived at the same time (same tick).
  */
@EventDriven
@Polling(concurrentConsumers = 3, maxConcurrentConsumers = 5)
class RegressionProcessor {

  @Resource
  private var space: GigaSpace = _

  @EventTemplate
  def template(): SQLQuery[MarketReturn] = {
    new SQLQuery[MarketReturn](MarketReturn.getClass.getName, "where processed = false")
  }

  @SpaceDataEvent
  def listener(marketReturn: MarketReturn): MarketReturn = {

    val marketReturnPct = marketReturn.percentageRateOfReturn
    val time1: Long = marketReturn.timestampMs
    val time0 = time1 - Settings.msPerMonth

    val investments = space.readMultiple(
      new SQLQuery[InvestmentReturn](
        classOf[InvestmentReturn],
        s"WHERE timestampMs <= $time1 AND timestampMs > $time0")
    ).groupBy(inv => inv.investmentId)

    val investmentIds = investments.keys
    for (invId <- investmentIds) {
      // TODO multi-Thread

      val forThisId = investments.getOrElse(invId, Array[InvestmentReturn]())

      val regressors =
        forThisId.map(
          ir => (ir.percentageRateOfReturn, marketReturnPct)
        )

      val (a, aVar, aConfMag, b, bVar, bConfMag, mVar) = SimpleRegressionModel
          .leastSquares(regressors)

      space.write(CharacteristicLine(
        id = null,
        timestampMs = time1,
        investmentId = invId,
        a = a,
        aVariance = aVar,
        aConfidenceIntervalMagnitude = aConfMag,
        b = b,
        bVariance = bVar,
        bConfidenceIntervalMagnitude = bConfMag,
        modelErrorVariance = mVar
      ))
    }

    marketReturn.processed = true
    marketReturn
  }

}
