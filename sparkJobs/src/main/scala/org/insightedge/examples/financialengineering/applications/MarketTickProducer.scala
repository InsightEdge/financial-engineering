package org.insightedge.examples.financialengineering.applications

import java.io.FileNotFoundException
import java.time.DayOfWeek.FRIDAY
import java.time.{ZoneId, ZonedDateTime}

import scala.annotation.tailrec
import scala.io.Source

import org.apache.kafka.clients.producer.{Producer, ProducerRecord}
import org.insightedge.examples.financialengineering.CoreSettings
import org.insightedge.examples.financialengineering.model.{MarketTick, TickerSymbol}

case class MarketTickProducer(baseDir: String, ts: TickerSymbol, producer: Producer[String, MarketTick]) extends Runnable {
  
  private val firstValue: Long = 19980106 // the start "date" of the data set we're using
  
  private val maxRetryCount = 5
  private var retry = 0
  
  def run() {
      processFile(calculateNextFilename(ts.getAbbreviation, firstValue), firstValue)
  }

  @tailrec
  private def processFile(filename: String, date: Long): Unit = {
    val abbr = ts.getAbbreviation
    var reader: Source = null
    try {
      reader = Source.fromFile(filename)
      reader.getLines().foreach { line =>
        if (!line.isEmpty) {
          producer.send(new ProducerRecord(abbr, abbr, MarketTick(line)))
        }
      }
      retry = 0
      println(s"Finished processing $filename file.")
    } catch {
      case e: FileNotFoundException => {
        if (retry == maxRetryCount) {
          println(s"Finished processing for $ts ticker symbol")
          return
        } else {
          println(s"File: $filename was not found. Try to process next file.")
          retry += 1
        }
      }
      case e1: Exception => {
        System.err.println(s"Error in processing $filename file. $ts ticker symbol failed to be processed.")
        return
      }
    } finally {
      if (reader != null) reader.close()
    }
    
    val newDate = calculateNextTickerSymbolDate(date)
    val newFileName = calculateNextFilename(abbr, newDate)
    println(s"About to process $newFileName file.")
    processFile(newFileName, newDate)
  }
  
  /**
    * This method is used to figure out what the next file is to load in the sequence.
    * File naming and layout are based upon the strategy used by our data provider.
    *
    * @param tickerSymbol a tracked ticker symbol
    * @return an unvalidated fully-qualified filename
    */
  private def calculateNextFilename(tickSymAbbr: String, date: Long): String = {
    val abbr = tickSymAbbr.toLowerCase()
    s"${baseDir}/allstocks_$date/table_$abbr.csv"
  }

  /**
   * @param date in format yyyymmdd
   * @return the next working day after the specified date in format yyyymmdd
   */
  private def calculateNextTickerSymbolDate(date: Long): Long = {  
    val yrMagnitude = 10000
    val monMagnitude = 100
    val year = date / yrMagnitude
    val nonYrRemainder = date % yrMagnitude
    val month = nonYrRemainder / monMagnitude
    val day = nonYrRemainder % monMagnitude
    
    val dateTime = ZonedDateTime.of(year.toInt, month.toInt, day.toInt, 0, 0, 0, 0, ZoneId.of(CoreSettings.timeZone))
    val nextDay = nextWorkingDay(dateTime)
    nextDay.getYear * yrMagnitude + nextDay.getMonthValue * monMagnitude + nextDay.getDayOfMonth
  }

  private def nextWorkingDay(date: ZonedDateTime) = date.plusDays(if (date.getDayOfWeek == FRIDAY) 3 else 1)
}