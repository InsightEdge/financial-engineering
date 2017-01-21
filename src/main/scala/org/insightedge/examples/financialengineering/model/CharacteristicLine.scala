package org.insightedge.examples.financialengineering.model

import com.gigaspaces.annotation.pojo.CompoundSpaceIndex
import org.insightedge.scala.annotation.{SpaceId, SpaceIndex}

import scala.beans.BeanProperty

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/21/17
  * Time: 12:44 PM
  */

@CompoundSpaceIndex(paths = Array("timestampMs", "investmentId"))
case class CharacteristicLine(
                               @SpaceId(autoGenerate = true)
                               @BeanProperty
                               var id: String,
                               @SpaceIndex
                               @BeanProperty
                               var timestampMs: Long,
                               @SpaceIndex
                               @BeanProperty
                               var investmentId: String,
                               @BeanProperty
                               var a: Double,
                               @BeanProperty
                               var aVariance: Double,
                               @BeanProperty
                               var alphaConfidenceIntervalMagnitude: Double,
                               @BeanProperty
                               var b: Double,
                               @BeanProperty
                               var bVariance: Double,
                               @BeanProperty
                               var betaConfidenceIntervalMagnitude: Double,
                               @BeanProperty
                               var modelErrorVariance: Double
                             ) {
  def this() = this(null, -1L, -1D, -1D, -1D, -1D, -1D, -1D, -1D, -1D)
}
