package org.insightedge.examples.financialengineering.model

import org.insightedge.scala.annotation.SpaceId

import scala.beans.BeanProperty

/**
 * User: jason
 *
 * Time: 2:27 PM
 */
case class TickerSymbol(@SpaceId(autoGenerate = false)
                        @BeanProperty var abbreviation: String) {
  def this() = this(null)

  override def toString = getAbbreviation
}
