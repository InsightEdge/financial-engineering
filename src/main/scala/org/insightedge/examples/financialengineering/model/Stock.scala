package org.insightedge.examples.financialengineering.model

import org.insightedge.scala.annotation.SpaceId

import scala.beans.BeanProperty

/** User: jason
  * Time: 10:20 PM
  */
case class Stock(@SpaceId(autoGenerate = true)
                 @BeanProperty
                 var id: String,
                 @BeanProperty
                 var tickerSymbol: String,
                 @BeanProperty
                 var outstandingShares: Long,
                 @BeanProperty
                 var cusipNumber: Int,
                 @BeanProperty
                 var lastUpdateMs: Long) {

  def this() = this(null, null, -1, -1, -1)

}
