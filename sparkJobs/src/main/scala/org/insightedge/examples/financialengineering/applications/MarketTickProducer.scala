package org.insightedge.examples.financialengineering.applications

import java.io.FileNotFoundException

import scala.annotation.tailrec
import scala.io.Source

import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.insightedge.examples.financialengineering.model.MarketTick
import org.insightedge.examples.financialengineering.model.TickerSymbol

case class MarketTickProducer(baseDir: String, ts: TickerSymbol, producer: Producer[String, MarketTick]) extends Runnable {
  
  private val firstValue: Long = 19980106 // the start "date" of the data set we're using
  
  private val maxRetryCount = 20
  private var retry = 0
  
  def run() {
      processFile(calculateNextFilename(ts.getAbbreviation, firstValue), firstValue)
  }

  @tailrec
  private def processFile(filename: String, timestamp: Long): Unit = {
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
        println(s"Error in processing $filename file. Try to process next file.")
      }
    } finally {
      if (reader != null) reader.close()
    }
    
    val newTs = calculateNextTickerSymbolTs(timestamp)
    val newFileName = calculateNextFilename(abbr, newTs)
    println(s"About to process $newFileName file.")
    processFile(newFileName, newTs)
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

  private def calculateNextTickerSymbolTs(curTs: Long): Long = {
    val yrMagnitude = 10 ^ 4
    val monMagnitude = 10 ^ 2
    val yr = curTs / yrMagnitude
    val leapYear = yr % 4 == 0
    val nonYrRemainder = curTs % yrMagnitude
    val cur = (leapYear, nonYrRemainder / monMagnitude, nonYrRemainder % monMagnitude)
    val monthToUse: Long = cur match {
      case (_, 1L | 3L | 5L | 7L | 9L | 10L | 12L, 31L) =>
        cur._2 + 1
      case (true, 2L, 28L) =>
        2
      case (false, 2L, 28L) =>
        3
      case (_, 4L | 6L | 8L | 11L, 30L) =>
        cur._2 + 1
      case (_, _, _) =>
        cur._2
    }
    val nextDay: Long = (leapYear, cur._2, cur._3) match {
      case (_, 1L | 3L | 5L | 7L | 9L | 10L | 12L, 31L) =>
        1
      case (false, 2L, 28L) =>
        1
      case (true, 2L, 28L) =>
        29
      case (_, 4L | 6L | 8L | 11L, 30L) =>
        1
      case (_, _, day) =>
        day + 1
    }
    val yearToUse = (cur._2, cur._3) match {
      case (12, 31) => yr + 1
      case (_, _) => yr
    }
    yearToUse * yrMagnitude + monthToUse * monMagnitude + nextDay
  }
}