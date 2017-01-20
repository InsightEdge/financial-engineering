package org.insightedge.examples.financialengineering.model

import org.insightedge.scala.annotation.SpaceId

import scala.beans.BeanProperty

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Time: 6:00 PM
``  */

case class RegressorResult(@SpaceId(autoGenerate = false)
                            @BeanProperty var id: String,
                            @BeanProperty var alpha: Double,
                            @BeanProperty var beta: Double,
                            @BeanProperty var epsilon: Double,
                            @BeanProperty var tickTimestampMs: Long) {
  def this() = this(null, -1, -1, -1, -1)

}


