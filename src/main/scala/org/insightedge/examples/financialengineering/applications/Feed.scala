package org.insightedge.examples.financialengineering.applications

import java.nio.file.{Files, Paths}

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.insightedge.examples.financialengineering
import org.insightedge.examples.financialengineering.kafka.MarketTickDecoder
import org.insightedge.examples.financialengineering.{Settings, SpaceUsage}
import org.insightedge.examples.financialengineering.model.{MarketTick, TickerSymbol, TickerSymbols}

import scala.collection.JavaConverters._
import scala.collection.concurrent.Map

/**
  * User: jason nerothin
  *
  * Time: 5:39 AM
  * Pulls data from a csv file and writes it to Kafka.
  *
  * The file has the ticker symbol in its name. It has a header line
  * and this format:
  *
  * date | time | open | high | low | close | volume | splits | earnings | dividends
  *
  */
object Feed extends SpaceUsage {

  val sleepTimeMilliseconds: Long = Settings.feedSleepTimeMs
  val producer = new KafkaProducer[String, MarketTick](financialengineering.kafkaProducerProperties())
  val encoder = new MarketTickDecoder
  val progressMarker: Map[TickerSymbol, Long] = new java.util.concurrent.ConcurrentHashMap[TickerSymbol, Long]().asScala

  /**
    * We need to run one of these for each tracked ticker symbol
    *
    * @param args ignored
    */
  def main(args: Array[String]): Unit = {

    val tickerSymbol = TickerSymbols.provideTickerSymbolForFeed()

    tickerSymbol match {
      case Some(symbol) =>

        var haveMoreData = true
        while (haveMoreData) {

          processNextFile(symbol) match {
            case None =>
              val abbr = symbol.abbreviation
              println(s"There are enough Feeder Threads for symbol [$abbr]. Exiting.")
              haveMoreData = false
            case Some(str) =>
              val abbr = symbol.abbreviation
              println(s"Data file [$str] for ticker symbol [$abbr] has been processed.")
              Thread.sleep(sleepTimeMilliseconds)
          }

        }
        TickerSymbols.returnSymbolFromFeed(symbol)

      case None =>
        println("No more ticker symbols need to be processed (each has its own Thread). Exiting.")
    }



  }

  /**
    * Processes a data file, sending its content to the Data Grid.
    *
    * @return Some filename or none if none such exists
    */
  private def processNextFile(symbol: TickerSymbol): Option[String] = {
    val abbr = symbol.abbreviation
    val filename = calculateNextFilename(symbol)
    if (Files.exists(Paths.get(filename))) {
      val reader = scala.io.Source.fromFile(filename).bufferedReader()
      var line: String = reader.readLine()
      while (line != null) {
        val buf = line.toBuffer
        if (buf.nonEmpty) {
          val tick = encoder.fromBytes(line.getBytes())
          val msg = new ProducerRecord[String, MarketTick](abbr, tick)
          producer.send(msg)
        }
        line = reader.readLine()
      }
      reader.close()
      Some(filename)
    } else {
      None
    }
  }

  /**
    * This method is used to figure out what the next file is to load in the sequence.
    * File naming and layout are based upon the strategy used by our data provider.
    *
    * @param tickerSymbol a tracked ticker symbol
    * @return an unvalidated fully-qualified filename
    */
  private def calculateNextFilename(tickerSymbol: TickerSymbol): String = {
    val basedir = Settings.feedDataDirectory
    val dateString = nextDateString(tickerSymbol)
    val abbr = tickerSymbol.abbreviation.toLowerCase()
    s"${basedir}allstocks_$dateString/table_$abbr.csv"
  }

  private val firstValue: Long = 19980106 // the start "date" of the data set we're using

  private def nextDateString(tickerSymbol: TickerSymbol): Long = {
    def newValue(value: Long): Long = {
      val yrMagnitude = 10 ^ 4
      val monMagnitude = 10 ^ 2
      val yr = value / yrMagnitude
      val leapYear = yr % 4 == 0
      val nonYrRemainder = value % yrMagnitude
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

    val nv: Long = progressMarker.get(tickerSymbol) match {
      case Some(current) =>
        newValue(current)
      case None =>
        firstValue
    }

    progressMarker.put(tickerSymbol, nv)
    nv

  }

}