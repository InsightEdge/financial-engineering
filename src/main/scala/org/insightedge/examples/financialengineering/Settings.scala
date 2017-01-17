package org.insightedge.examples.financialengineering

import java.time.{LocalDateTime, ZoneId}

/**
  *
  * User: jason
  *
  * Time: 7:16 PM
  * Central location for common settings.
  */
object Settings {



  val timeZone: String = "America/New_York"
  val daysPerYear: Short = 360
  val msPerDay: Long = 24 * 60 * 1000
  val msPerMonth: Long = 30 * msPerDay

  val tradingDaysPerMonth: Short = 21
  val ticksPerDay: Int = 440
  val ticksPerMonth: Long = ticksPerDay * tradingDaysPerMonth

  val ingestionThreadsPerSymbol = 1
  val ingestionFrequencyMilliseconds = 1
  val ingestionAppName = "KafkaToSpaceIngestion"
  val ingestContextFrequencyMilliseconds = 2000

  val feedAppName = "TickDataFromCsvFeed"
  val feedSleepTimeMs = 2000
  val feedDataDirectory = "/tmp/marketdata/"

  val processorAppName = "ProcessTicks"
  val processTicksThreads = 1
  val processTickFrequencyMs = 750

  val tickerSymbolLimit = 1
  val tickerSymbolsFilename = "/tmp/41_symbols.csv"

  // Data Grid
  val spaceName = "fin-eng-space"
  val spaceLookupGroups = "xap-12.1.0"
  // private val spaceLookupLocators = "localhost:7102"
  val spaceLookupLocators = "127.0.0.1:4174"
  val remoteJiniUrl = s"jini://$spaceLookupLocators/*/$spaceName"

  // Spark
  val sparkMasterUrl = "local[7]"

  // Kafka
  val kafkaBrokers = "127.0.0.1:9092"

}