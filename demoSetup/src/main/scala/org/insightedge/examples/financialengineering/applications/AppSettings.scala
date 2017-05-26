package org.insightedge.examples.financialengineering.applications

import com.typesafe.config.ConfigFactory

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 3/30/17
  * Time: 12:02 PM
  */
object AppSettings {

   val config = ConfigFactory.load()
   
   def getTickerSymbolLimit() = if (config.hasPath("tickerSymbol.limit")) config.getInt("tickerSymbol.limit") else Int.MaxValue
   def getTickerSymbolsFilename() = config.getString("tickerSymbol.filename")
}
