package org.insightedge.examples.financialengineering

/**
  *
  * User: jason
  *
  * Time: 7:16 PM
  * Central location for common settings.
  */
object CoreSettings {

  val timeZone: String = "America/New_York"
  val daysPerYear: Short = 360
  val msPerDay: Long = 24 * 60 * 60 * 1000
  val msPerMonth: Long = 30 * msPerDay

  val tradingDaysPerMonth: Short = 21
  val ticksPerDay: Int = 440
  val ticksPerMonth: Long = ticksPerDay * tradingDaysPerMonth

  val feedAppName = "TickDataFromCsvFeed"

  val processorAppName = "ProcessTicks"
  val processTicksThreads = 1
  val processTickFrequencyMs = 750

  val ingestionThreadsPerSymbol = 1
  val calcIndividualThreadsPerSymbol = 1
  val calcMarketReturnThreadsPerSymbol = 1

  val spaceName = "finEngSpace"
  val spaceLookupGroups = "xap-12.0.0"
  // private val spaceLookupLocators = "localhost:7102"
  val spaceLookupLocators = "localhost"
//  val spaceLookupLocators = "macattack"
  val remoteJiniUrl = s"jini://$spaceLookupLocators/*/$spaceName?groups=$spaceLookupGroups"

}