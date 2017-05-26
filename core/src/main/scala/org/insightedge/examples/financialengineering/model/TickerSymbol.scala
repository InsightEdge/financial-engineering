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

  override def equals(that: Any): Boolean =
    that match {
      case that: TickerSymbol => that.canEqual(this) && this.abbreviation == that.abbreviation
      case _ => false
    }

  override def hashCode: Int = getAbbreviation.hashCode()

  override def toString = getAbbreviation
}
