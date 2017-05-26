package org.insightedge.examples.financialengineering.model

import org.insightedge.scala.annotation.{SpaceId}

import scala.beans.{BeanProperty, BooleanBeanProperty}

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
                     @BooleanBeanProperty
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
object TickData {

  def apply(sym: String, t: MarketTick) = {
     new TickData(id = null,
        symbol = sym,
        timestampMs = t.timestamp,
        close = t.close,
        volume = t.volume,
        earnings = t.earnings,
        dividends = t.dividends,
        open = t.open,
        processed = false
      )
  }
}
