package org.insightedge.examples.financialengineering.model

import org.insightedge.scala.annotation.{SpaceId, SpaceIndex}

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
                         @SpaceIndex
                         @BeanProperty
                         var timestampMs: Long,
                         @BeanProperty
                         var percentageRateOfReturn: Double,
                         @BeanProperty
                         var variance: Double,
                         @BeanProperty
                         var processed: Boolean = false
                       ) {
  def this() = this(null, -1, -1, -1, false)
}

/**
  *
  * Jobs:
  *
  * IndividualReturn  (x / symbol) // Jason
  * MarketReturn (y / tick) // ?? Sudip
  * PollingContainer?? for (a,b,e)(symbol)(t) ??? // Jason
  *
  */
