package org.insightedge.examples.financialengineering.applications

import org.insightedge.examples.financialengineering.SpaceUsage
import org.insightedge.examples.financialengineering.model.TickerSymbol
import org.insightedge.examples.financialengineering.repos.TickerSymbols
import org.openspaces.core.GigaSpace
import org.openspaces.core.EntryAlreadyInSpaceException
import com.gigaspaces.client.WriteModifiers
import com.j_spaces.core.LeaseContext
import scala.io.Source;

/**
 *
 * User: jason
 *
 * Time: 7:25 AM
 * To start processing new data for new ticker symbols, run this application.
 */
object TickerSymbolCustomizer extends SpaceUsage {

  val space: GigaSpace = makeClusteredProxy()

  /**
   *  Attempts to add the symbol to the Data Grid.
   *
   * @param symbol the one we're trying to add.
   * @return [[None]] if successfu2l. If that symbol was stored previously, it is returned.
   */
  def tryToAddSymbol(symbol: TickerSymbol): Option[TickerSymbol] = {
    val abbr = symbol.getAbbreviation
    try {
      space.write(symbol, WriteModifiers.WRITE_ONLY)
      println(s"Created Ticker Symbol '$abbr' in the space")
      None
    } catch {
      case e: EntryAlreadyInSpaceException => {
        println(s"Symbol $abbr already exists in the space")
        Some(symbol)
      }
    }
  }

  def main(args: Array[String]): Unit = {
    var reader : Source = null

    try {
      reader = Source.fromInputStream(getClass.getResourceAsStream("/" + (if (args.length > 0) args(0) else AppSettings.getTickerSymbolsFilename)))
      val limit = AppSettings.getTickerSymbolLimit
      println(s"Adding $limit new TickerSymbols to the system")
      val lines = reader.getLines().take(limit).foreach { l =>
        val abbr = l.split(",")(0).toUpperCase
        println(s"Adding TickerSymbol '$abbr' to the system")
        tryToAddSymbol(new TickerSymbol(abbr))
      }
    } finally {
      if (reader != null) {
        reader.close()
      }
    }
  }
}
