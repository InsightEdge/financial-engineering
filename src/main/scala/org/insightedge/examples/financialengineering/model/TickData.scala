package org.insightedge.examples.financialengineering.model

import org.insightedge.scala.annotation.SpaceId

import scala.beans.BeanProperty

/**
  *
  * User: jason
  *
  * Time: 10:32 AM
  */
case class TickData(
                     @SpaceId(autoGenerate = true)
                     @BeanProperty
                     var id: String,
                     @BeanProperty
                     var symbol: String,
                     @BeanProperty
                     var timestampMs: Long,
                     @BeanProperty
                     var close: Double,
                     @BeanProperty
                     var volume: Double,
                     @BeanProperty
                     var earnings: Double,
                     @BeanProperty
                     var dividends: Double,
                     @BeanProperty
                     var open: Double,
                     @BeanProperty
                     var processed: Boolean
                   ) {

  def this() = this(id = null,
    symbol = null,
    timestampMs = 0L,
    close = -1d,
    volume = -1L,
    earnings = -1d,
    dividends = -1d,
    open = -1d,
    processed = false)

}

object TickDataHelp {

  implicit class Help(d: TickData) {

    def isEarlierThan(d2: TickData): Boolean = {
      d2.timestampMs > d.timestampMs
    }

    def isSameTime(d2: TickData): Boolean = {
      d2.timestampMs == d.timestampMs
    }

    def isLaterThan(d2: TickData): Boolean = {
      d2.timestampMs > d.timestampMs
    }

    def toInvestment(d2: TickData): Investment = {
      var first:TickData = null
      var second:TickData = null
      if (d2.isEarlierThan(d)) {
        first = d2
        second = d
      } else{
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
  }

}
