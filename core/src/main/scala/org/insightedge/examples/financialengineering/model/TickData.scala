package org.insightedge.examples.financialengineering.model

import org.insightedge.scala.annotation.{SpaceId, SpaceRouting}

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
                     @SpaceRouting
                     var symbol: String,
                     @BeanProperty
                     var timestampMs: Long,
                     @BeanProperty
                     var dateAsStr: String,
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
                     var high: Double,
                     @BeanProperty
                     var low: Double
                   ) {

  def this() = this(id = null,
    symbol = null,
    timestampMs = 0L,
    dateAsStr = null,
    close = -1d,
    volume = -1L,
    earnings = -1d,
    dividends = -1d,
    open = -1,
    high = -1,
    low = -1)
}
object TickData {

  def apply(sym: String, t: MarketTick) = {
     new TickData(id = null,
        symbol = sym,
        timestampMs = t.timestamp,
        dateAsStr = t.dateAsStr,
        close = t.close,
        volume = t.volume,
        earnings = t.earnings,
        dividends = t.dividends,
        open = t.open,
        high = t.high,
        low = t.low
      )
  }
}
