package org.insightedge.examples.financialengineering.model

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import org.insightedge.examples.financialengineering.CoreSettings

/**
  *
  * User: jason
  *
  * Time: 10:19 AM
  *
  */
case class MarketTick(timestamp: Long, dateAsStr: String, open: Double, high: Double, low: Double, close: Double, volume: Double, splits: Double, earnings: Double, dividends: Double) {
   
  override def toString = s"$timestamp,$dateAsStr,$open,$high,$low,$close,$volume,$splits,$earnings,$dividends"

}

object MarketTick {
  
  def calculateTimestamp(date: String, time: String): ZonedDateTime = {
    val (year, monthAndDay) = date.splitAt(4)
    val (month, day) = monthAndDay.splitAt(2)
    val (hour, minute) = time.length match {
      case 3 => time splitAt 1
      case 4 => time splitAt 2
    }
    ZonedDateTime.of(year.toInt, month.toInt, day.toInt, hour.toInt, minute.toInt, 0, 0, ZoneId.of(CoreSettings.timeZone))
  }

  /**
   * Content in the following format:
   *  0    1    2    3    4   5     6      7      8        9  
   * date,time,open,high,low,close,volume,splits,earnings,dividends
   */
  def apply(content: String) = {
    val values = content.split(",")
    val date = calculateTimestamp(values(0), values(1))
    val dateAsStr = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date)
    new MarketTick(
      date.toInstant.toEpochMilli(),
      dateAsStr,
      open = values(2).toDouble,
      high = values(3).toDouble,
      low = values(4).toDouble,
      close = values(5).toDouble,
      volume = values(6).toDouble,
      splits = values(7).toDouble,
      earnings = values(8).toDouble,
      dividends = if (values.length == 10) values(9).toDouble else 0
    )
  }
}
