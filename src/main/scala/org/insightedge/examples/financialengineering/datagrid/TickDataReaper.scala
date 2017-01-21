package org.insightedge.examples.financialengineering.datagrid

import javax.annotation.Resource

import com.gigaspaces.query.IdQuery
import com.j_spaces.core.client.SQLQuery
import org.insightedge.examples.financialengineering.model.TickData
import org.openspaces.core.GigaSpace
import org.openspaces.events.adapter.SpaceDataEvent
import org.openspaces.events.polling.Polling
import org.openspaces.events.{EventDriven, EventTemplate}

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/20/17
  * Time: 11:30 PM
  */
@EventDriven
@Polling
class TickDataReaper {

  @Resource
  private var space: GigaSpace = _

  @EventTemplate
  def template(): SQLQuery[TickData] = {
    new SQLQuery[TickData](TickData.getClass.getName, "WHERE processed = true")
  }

  @SpaceDataEvent
  def listener(td: TickData): TickData = {
    space.takeById(new IdQuery(classOf[TickData], td.id))
    null
  }

}
