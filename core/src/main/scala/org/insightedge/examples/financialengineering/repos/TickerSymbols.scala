package org.insightedge.examples.financialengineering.repos

import com.gigaspaces.query.IdQuery
import com.j_spaces.core.client.SQLQuery
import org.insightedge.examples.financialengineering.{ CoreSettings, SpaceUsage }
import org.insightedge.examples.financialengineering.model.TickerSymbol
import org.openspaces.core.GigaSpace

import scala.reflect.classTag
/**
 *
 * User: jason
 *
 * Time: 2:29 PM
 * A repository for [[org.insightedge.examples.financialengineering.model.TickerSymbol]]s
 */

object TickerSymbols extends SpaceUsage {

  private val space: GigaSpace = makeClusteredProxy()
  private val clazz = classTag[TickerSymbol].runtimeClass.asInstanceOf[Class[TickerSymbol]]

  def all(): Seq[TickerSymbol] = {
    val qry = new SQLQuery[TickerSymbol](clazz, "")
    space.readMultiple(qry).toSeq
  }
  
  def count(): Int = {
    space.count(new TickerSymbol)
  }

  /**
   * Makes an [[com.gigaspaces.query.IdQuery]] for [[TickerSymbol]]s
   *
   * @param tickerSymbol for which to create one
   * @return such a query
   */
  def makeIdQuery(tickerSymbol: TickerSymbol): IdQuery[TickerSymbol] = {
    new IdQuery[TickerSymbol](clazz, tickerSymbol.getAbbreviation)
  }
  
}