package org.insightedge.examples.financialengineering.model

import org.insightedge.scala.annotation.SpaceId

import scala.beans.BeanProperty

/**
  *
  * User: jason
  *
  * Time: 2:27 PM
  */
case class TickerSymbol(@SpaceId(autoGenerate = false)
                        @BeanProperty
                        var abbreviation: String,
                        @BeanProperty
                        var ingestionThreadCount: Int,
                        @BeanProperty
                        var tickProcessorThreadCount: Int,
                        @BeanProperty
                        var feedThreadCount: Int) {
  def this() = this(null, ingestionThreadCount = -1, tickProcessorThreadCount = -1, feedThreadCount = -1)

  override def toString: String = {
    abbreviation
  }
}

trait TickerSymbolProperties {
  val ingestionPropertyName = "ingestionThreadCount"
  val tickPropertyName = "tickProcessorThreadCount"
  val feedPropertyName = "feedThreadCount"
}

object TickerSymbolProperties extends TickerSymbolProperties
