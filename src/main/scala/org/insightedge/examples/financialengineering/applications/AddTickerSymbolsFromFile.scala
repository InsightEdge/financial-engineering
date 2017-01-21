package org.insightedge.examples.financialengineering.applications

import org.insightedge.examples.financialengineering.model.{TickerSymbol, TickerSymbols}
import org.insightedge.examples.financialengineering.{Settings, SpaceUsage}
import org.openspaces.core.GigaSpace


/**
  *
  * User: jason
  *
  * Time: 7:25 AM
  * To start processing new data for new ticker symbols, run this application.
  */
object AddTickerSymbolsFromFile extends SpaceUsage {

  val space: GigaSpace = makeClusteredProxy()

  /** Attempts to add the symbol to the Data Grid.
    *
    * @param symbol the one we're trying to add.
    * @return [[None]] if successful. If that symbol was stored previously, it is returned.
    */
  def tryToAddSymbol(symbol: TickerSymbol): Option[TickerSymbol] = {
    val abbr = symbol.abbreviation
    val existingSymbol = space.readById(TickerSymbols.makeIdQuery(symbol))
    if (existingSymbol != null) {
      println(s"Symbol $abbr already exists in the Space.")
      println(s"Details: $symbol")
      Some(existingSymbol)
    }
    else {
      space.write(symbol)
      println(s"Created Ticker Symbol '$abbr' in the Space.")
      None
    }
  }

  def main(args: Array[String]): Unit = {

    val reader = scala.io.Source.fromFile(Settings.tickerSymbolsFilename).bufferedReader()
    var lineCount = 0
    var line = ""
    val limit = Settings.tickerSymbolLimit
    println(s"Adding $limit new TickerSymbols to the system")
    line = reader.readLine()
    while (line != null && lineCount < limit) {
      lineCount = lineCount + 1
      val words = line.split(",")
      val abbr = words(0).toUpperCase()
      val symbol = TickerSymbol(
        abbr,
        ingestionThreadCount = 0,
        feedThreadCount = 0,
        calcIndividualReturnThreadCount = 0,
        calcMarketReturnThreadCount = 0
      )
      println(s"Adding TickerSymbol '$abbr' to the System.")
      tryToAddSymbol(symbol)
      line = reader.readLine()
    }
    println(s"Added $lineCount TickerSymbols to the Space.")

  }
}
