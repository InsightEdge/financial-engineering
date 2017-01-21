package org.insightedge.examples.financialengineering.model

import org.apache.spark.SparkContext
import org.insightedge.spark.context.InsightEdgeSparkContext
import org.insightedge.spark.implicits.BasicImplicits

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/20/17
  * Time: 8:29 PM
  * A repository for [[Stock]]s
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
