package org.insightedge.examples.financialengineering.model

import org.insightedge.examples.financialengineering.Settings
import org.insightedge.scala.annotation.SpaceId

import scala.beans.BeanProperty

/** User: jason
  *
  * Time: 4:07 AM
  * An Investment that occurred in the past.
  */
case class Investment(@SpaceId(autoGenerate = true)
                      @BeanProperty
                      var id: String,
                      @BeanProperty
                      var buyPrice: Double,
                      @BeanProperty
                      var sellPrice: Double,
                      @BeanProperty
                      var startDateMs: Long, // tickTime - 1 mo
                      @BeanProperty
                      var endDateMs: Long, // tickTime
                      @BeanProperty
                      var buyCost: Double = 0,
                      @BeanProperty
                      var sellCost: Double = 0,
                      @BeanProperty
                      var dividendsDuringTerm: Double = 0,
                      @BeanProperty
                      var exchange: String = "NYSE") {
  def this() = this(null, -1, -1, -1, -1, -1, -1, -1, null)
}

object InvestmentHelp {

  import java.time.Duration

  implicit class Help(i: Investment) {

    def duration(): Duration = {
      // 1440000 / 518400000
      Duration.ofMillis(i.endDateMs - i.startDateMs)
    }

    def years(): Double = {
      (i.endDateMs - i.startDateMs) / (Settings.daysPerYear * Settings.msPerDay.toDouble)
    }

    def compoundAnnualGrowthRate(): Double = {
      math.pow((i.sellPrice + i.dividendsDuringTerm - i.buyCost - i.sellCost) / i.buyPrice, 1 / years()) - 1
    }

    def CAGR(): Double = {
      compoundAnnualGrowthRate()
    }

  }

}