package org.insightedge.examples.financialengineering.model

import org.insightedge.examples.financialengineering.Settings
import org.insightedge.scala.annotation.SpaceId

import scala.beans.BeanProperty

/**
  *
  * User: jason
  *
  * Time: 4:07 AM
  * An Investment that occurred in the past.
  */
case class Trade(@SpaceId(autoGenerate = true)
                 @BeanProperty
                 var id: String,
                 @BeanProperty
                 var buyPrice: Double,
                 @BeanProperty
                 var sellPrice: Double,
                 @BeanProperty
                 var buyDateMs: Long,
                 @BeanProperty
                 var endDateMs: Long,
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

object TradeHelp {

  import java.time.Duration

  implicit class Help(i: Trade) {

    def duration(): Duration = {
      Duration.ofMillis(i.endDateMs - i.buyDateMs)
    }

    def months(): Double = {
      duration().toMillis / Settings.msPerMonth
    }

  }

}