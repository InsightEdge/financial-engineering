package org.insightedge.examples.financialengineering.model

import org.insightedge.scala.annotation.SpaceId

import scala.beans.BeanProperty

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/7/17
  * Time: 10:12 PM
  *
  * A record for a Market Rate of Return at a given point in time.
  */
case class MarketReturn(
                         @SpaceId(autoGenerate = true)
                         @BeanProperty
                         var id: String,
                         @BeanProperty
                         var timestampMs: Long,
                         @BeanProperty
                         var percentageRateOfReturn: Double
                       ) {
  def this() = this(null, -1, -1)

}
