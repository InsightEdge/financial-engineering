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

  val ticksWindowMs: Long = 3 * msPerDay
  val tradingDaysPerMonth: Short = 21
  val ticksPerDay: Int = 440
  val ticksPerMonth: Long = ticksPerDay * tradingDaysPerMonth

  val spaceName = "finEngSpace"
  val spaceLookupGroups = "xap-12.0.0"
  val spaceLookupLocators = "localhost"
  
  val characterLineFrequency = 100
  val confidenceIntervalAlpha = 0.05

  val remoteJiniUrl = s"jini://$spaceLookupLocators/*/$spaceName?groups=$spaceLookupGroups"
}