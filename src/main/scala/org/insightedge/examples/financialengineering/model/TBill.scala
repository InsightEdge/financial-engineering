package org.insightedge.examples.financialengineering.model

import org.insightedge.examples.financialengineering.Settings
import org.insightedge.scala.annotation.SpaceId

import scala.beans.BeanProperty

/**
  *
  * User: jason
  *
  * Time: 5:19 AM
  *
  * Pretty much mean Us Govt security...
  * Could be: {T-Bill, T-Bond, ...}
  */
case class TBill(@SpaceId(autoGenerate = true)
                 @BeanProperty
                 var id: String,
                 @BeanProperty
                 var coupon: Double,
                 @BeanProperty
                 var parValue: Double,
                 @BeanProperty
                 var termMs: Long,
                 @BeanProperty
                 var maturityDateMs: Long,
                 @BeanProperty
                 var cusipNumber: Int,
                 @BeanProperty
                 var timestampMs: Long){

  def this() = this(null, -1, -1, -1, -1, -1, -1)

}

object TBillHelp {

  implicit class Help(t: TBill) {
    def discountYield(purchasePrice: Double): Double = {
      (t.parValue - purchasePrice) / (t.parValue * Settings.daysPerYear)
    }
  }

}