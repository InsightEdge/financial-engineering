package org.insightedge.examples.financialengineering.model

import org.insightedge.scala.annotation.SpaceId

import scala.beans.BeanProperty

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/21/17
  * Time: 12:44 PM
  */

case class CharacteristicLine(
                               @SpaceId(autoGenerate = true)
                               @BeanProperty
                               var id: String,
                               @BeanProperty
                               var timestampMs: Long,
                               @BeanProperty
                               var investmentId: String,
                               @BeanProperty
                               var a: Double,
                               @BeanProperty
                               var aVariance: Double,
                               @BeanProperty
                               var aConfidenceIntervalMagnitude: Double,
                               @BeanProperty
                               var b: Double,
                               @BeanProperty
                               var bVariance: Double,
                               @BeanProperty
                               var bConfidenceIntervalMagnitude: Double,
                               @BeanProperty
                               var modelErrorVariance: Double
                             ) {
  def this() = this(null, -1L, null, -1D, -1D, -1D, -1D, -1D, -1D, -1D)
}
