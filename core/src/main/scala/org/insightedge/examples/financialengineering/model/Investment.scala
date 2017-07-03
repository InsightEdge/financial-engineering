package org.insightedge.examples.financialengineering.model

import org.insightedge.examples.financialengineering.CoreSettings

/** User: jason
  *
  * Time: 4:07 AM
  * An Investment that occurred in the past.
  */
case class Investment(buyPrice: Double,
                      sellPrice: Double,
                      startDateMs: Long, // tickTime - 1 mo
                      endDateMs: Long, // tickTime
                      buyCost: Double = 0,
                      sellCost: Double = 0,
                      dividendsDuringTerm: Double = 0,
                      exchange: String = "NYSE") {}

object Investment {
  def apply(d: TickData, d2: TickData) = {
    val (first, second) = if (d.timestampMs > d2.timestampMs) (d2, d) else (d, d2)
    
    new Investment(
      buyPrice = first.close,
      startDateMs = first.timestampMs,
      sellPrice = second.open,
      endDateMs = second.timestampMs
    )
  }
}

object InvestmentHelp {

  import java.time.Duration

  implicit class Help(i: Investment) {

    def duration(): Duration = Duration.ofMillis(i.endDateMs - i.startDateMs)

    def years(): Double = (i.endDateMs - i.startDateMs) / (CoreSettings.daysPerYear * CoreSettings.msPerDay.toDouble)

    def compoundAnnualGrowthRate(): Double = math.pow((i.sellPrice + i.dividendsDuringTerm - i.buyCost - i.sellCost) / i.buyPrice, 1 / years()) - 1
  }
}