package org.insightedge.examples.financialengineering.repos

import org.insightedge.examples.financialengineering.model.{Stock, TickerSymbol}
import org.insightedge.spark.context.InsightEdgeSparkContext

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/20/17
  * Time: 8:29 PM
  * A repository for [[org.insightedge.examples.financialengineering.model.Stock]]s
  */
class Stocks

object Stocks extends Stocks{

  def retrieveStock(tickerSymbol: TickerSymbol)(implicit sparkContext: InsightEdgeSparkContext): Stock = {
    val sym = tickerSymbol.abbreviation
    val STOCKS = sparkContext.gridSql[Stock](s"WHERE tickerSymbol = $sym").toLocalIterator.toList
    require(STOCKS.nonEmpty, s"No Stock found for symbol: $sym")
    val stock = STOCKS.head
    stock
  }

}
