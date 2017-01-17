package org.insightedge.examples.financialengineering.model

import com.gigaspaces.annotation.pojo.SpaceIndex
import org.insightedge.scala.annotation.SpaceId

import scala.beans.BeanProperty

/**
  *
  * User: jason
  *
  * Time: 10:19 AM
  *
  * date | time | open | high | low | close | volume | splits | earnings | dividends.
  */
case class MarketTick(
  @SpaceId(autoGenerate = true)
  @BeanProperty
  var id: String,
  @BeanProperty
  @SpaceIndex
  var timestamp: Long,
  @BeanProperty
  var open: Double,
  @BeanProperty
  var high: Double,
  @BeanProperty
  var low: Double,
  @BeanProperty
  var close: Double,
  @BeanProperty
  var volume: Double,
  @BeanProperty
  var splits: Double,
  @BeanProperty
  var earnings: Double,
  @BeanProperty
  var dividends: Double){

    def this() = this(null, 0, 0, 0, 0, 0, 0, 0, 0, 0)

}
