package org.insightedge.examples.financialengineering.datagrid

import com.j_spaces.core.client.SQLQuery
import org.insightedge.examples.financialengineering.model.MarketReturn
import org.openspaces.events.adapter.SpaceDataEvent
import org.openspaces.events.{EventDriven, EventTemplate}
import org.openspaces.events.asyncpolling.AsyncPolling
import org.openspaces.events.polling.Polling

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/20/17
  * Time: 12:23 PM
  * When Spark saves a MarketReturn to the Data Grid, this
  * Polling Container notices it, and calculates a Security
  * Characteristic Line for all IndividualReturns that
  * arrived at the same time (same tick).
  */
@EventDriven
@Polling(concurrentConsumers = 3, maxConcurrentConsumers = 5)
class MarketReturnProcessor {

  @EventTemplate
  def template(): SQLQuery[MarketReturn] = {
    new SQLQuery[MarketReturn](MarketReturn.getClass.getName, "where processed = false")
  }

  @SpaceDataEvent
  def listener(marketReturn: MarketReturn): MarketReturn = ???

}
