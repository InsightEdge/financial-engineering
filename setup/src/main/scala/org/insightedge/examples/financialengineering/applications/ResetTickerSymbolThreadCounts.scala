package org.insightedge.examples.financialengineering.applications

import com.gigaspaces.client.ChangeSet
import org.insightedge.examples.financialengineering.SpaceUsage
import org.insightedge.examples.financialengineering.model.TickerSymbolProperties
import org.insightedge.examples.financialengineering.repos.TickerSymbols


/**
  *
  * User: jason
  *
  * Time: 7:25 AM
  * Updates all TickerSymbols in the Space, setting their thread count fields back to zero.
  */
object ResetTickerSymbolThreadCounts extends SpaceUsage {

  def main(args: Array[String]): Unit = {
    TickerSymbols.all() foreach { sym =>
      val abbr = sym.abbreviation
      val qry = TickerSymbols.makeIdQuery(sym)
      val set = new ChangeSet()
        .set(TickerSymbolProperties.feedPropertyName, 0)
        .set(TickerSymbolProperties.calcIndividualPropertyName, 0)
        .set(TickerSymbolProperties.calcMarketReturnPropertyName, 0)
        .set(TickerSymbolProperties.tickPropertyName, 0)
      println(s"Resetting TickerSymbol Thread counts for '$abbr' in space.")
      makeClusteredProxy().change(qry, set)
    }
  }
}
