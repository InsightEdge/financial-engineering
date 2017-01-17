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
                     @BeanProperty var open: Double
                   ) {

  def this() = this(id = null, symbol = null, timestampMs = 0L, close = 0d, volume = 0L, earnings = 0d, dividends = 0d, open = 0d)

}
