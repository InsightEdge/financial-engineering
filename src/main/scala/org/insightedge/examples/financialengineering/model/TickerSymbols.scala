package org.insightedge.examples.financialengineering.model

import com.gigaspaces.client.ChangeSet
import com.gigaspaces.query.IdQuery
import com.j_spaces.core.client.SQLQuery
import org.insightedge.examples.financialengineering.{Settings, SpaceUsage}
import org.openspaces.core.GigaSpace

import scala.reflect.classTag
import scala.util.Random

/**
  *
  * User: jason
  *
  * Time: 2:29 PM
  * A repository for [[TickerSymbol]]s
  */
class TickerSymbols

object TickerSymbols extends SpaceUsage {

  val random = new Random(System.currentTimeMillis())
  val space: GigaSpace = makeClusteredProxy()

  private val tickPropName: String = TickerSymbolProperties.tickPropertyName
  private val feedPropName: String = TickerSymbolProperties.feedPropertyName
  private val ingestionPropName: String = TickerSymbolProperties.ingestionPropertyName
  private val clazz = classTag[TickerSymbol].runtimeClass.asInstanceOf[Class[TickerSymbol]]

  def all(): List[TickerSymbol] = {
    // List(TickerSymbol("F"), TickerSymbol("KO"), TickerSymbol("GE"))
    val qry = new SQLQuery[TickerSymbol](clazz, "")
    val symbolsArray: Array[TickerSymbol] = space.readMultiple(qry)
    symbolsArray.toList
  }

  def provideTickerSymbolForTickProcessor(): Option[TickerSymbol] = {
    provideSymbol(tickPropName)
  }

  def provideTickerSymbolForFeed(): Option[TickerSymbol] = {
    provideSymbol(feedPropName)
  }

  def provideTickerSymbolForIngestion(): Option[TickerSymbol] = {
    provideSymbol(ingestionPropName)
  }

  private def provideSymbol(propertyName: String): Option[TickerSymbol] = {
    val available = all()
      .filter(!isLocked(_, propertyName))
//      .filter(_.abbreviation == "GT") // TODO delete
    val len = available.length
    if (len == 0) None
    else {
      val selected = available(random.nextInt(len))
      increment(selected, propertyName)
      Some(selected)
    }
  }

  // TODO this file is screaming out for currying and/or other functional techniques to simplify/dedup
  private def decrementIngestionThreadCount(tickerSymbol: TickerSymbol): Unit = {
    space.asyncChange(makeIdQuery(tickerSymbol), new ChangeSet().decrement(ingestionPropName, 1))
    tickerSymbol.ingestionThreadCount = tickerSymbol.ingestionThreadCount + 1
  }

  private def decrementFeedThreadCount(tickerSymbol: TickerSymbol): Unit = {
    space.asyncChange(makeIdQuery(tickerSymbol), new ChangeSet().decrement(ingestionPropName, 1))
    tickerSymbol.feedThreadCount = tickerSymbol.feedThreadCount + 1
  }

  private def decrementTickProcessorThreadCount(tickerSymbol: TickerSymbol): Unit = {
    space.asyncChange(makeIdQuery(tickerSymbol), new ChangeSet().decrement(tickPropName, 1))
    tickerSymbol.tickProcessorThreadCount = tickerSymbol.tickProcessorThreadCount + 1
  }

  private def decrement(tickerSymbol: TickerSymbol, propertyName: String): Unit = {
    propertyName match {
      case `ingestionPropName` =>
        decrementIngestionThreadCount(tickerSymbol)
      case `feedPropName` =>
        decrementFeedThreadCount(tickerSymbol)
      case `tickPropName` =>
        decrementTickProcessorThreadCount(tickerSymbol)
      case _ =>
        val msg = s"The '$propertyName' type is not supported by this implementation."
        System.out.println(msg)
        throw new IllegalStateException()
    }
  }

  private def returnSymbol(tickerSymbol: TickerSymbol, propertyName: String): Unit = {
    decrement(tickerSymbol, propertyName)
  }

  def returnSymbolFromIngestion(tickerSymbol: TickerSymbol): Unit = {
    returnSymbol(tickerSymbol, ingestionPropName)
  }

  def returnSymbolFromTickProcessor(tickerSymbol: TickerSymbol): Unit = {
    returnSymbol(tickerSymbol, tickPropName)
  }

  def returnSymbolFromFeed(symbol: TickerSymbol):Unit = {
    returnSymbol(symbol, feedPropName)
  }

  private def isLocked(tickerSymbol: TickerSymbol, propertyName: String): Boolean = {
    propertyName match {
      case `ingestionPropName` =>
        tickerSymbol.ingestionThreadCount >= Settings.ingestionThreadsPerSymbol
      case `feedPropName` =>
        tickerSymbol.feedThreadCount >= 1
      /* Until individual file processing is multi-threaded, we'll leave feed
       * thread limit as 1 per symbol to prevent errors
       */
      case `tickPropName` =>
        tickerSymbol.tickProcessorThreadCount >= Settings.processTicksThreads
      case _ =>
        throw new IllegalStateException(s"No property name match for $propertyName.")
    }
  }

  private def increment(tickerSymbol: TickerSymbol, propertyName: String): Unit = {
    propertyName match {
      case `ingestionPropName` =>
        incrementIngestionThreadCount(tickerSymbol)
      case `feedPropName` =>
        incrementFeedThreadCount(tickerSymbol)
      case `tickPropName` =>
        incrementTickProcessorThreadCount(tickerSymbol)
      case _ =>
        val msg = s"The '$propertyName' type is not supported by this implementation."
        System.out.println(msg)
        throw new IllegalStateException()
    }
  }

  private def provideSymbol(propertyName: String, lock: (TickerSymbol, String) => Unit): Option[TickerSymbol] = {
    val available = all().filter(isLocked(_, propertyName))
    val len = available.length
    if (len == 0) None
    else {
      val selected = available(random.nextInt())
      lock(selected, propertyName)
      Some(selected)
    }
  }

  private def incrementIngestionThreadCount(tickerSymbol: TickerSymbol) {
    space.asyncChange(makeIdQuery(tickerSymbol), new ChangeSet().increment(ingestionPropName, 1))
    tickerSymbol.ingestionThreadCount = tickerSymbol.ingestionThreadCount + 1
  }

  /**
    * Makes an [[com.gigaspaces.query.IdQuery]] for [[TickerSymbol]]s
    *
    * @param tickerSymbol for which to create one
    * @return such a query
    */
  def makeIdQuery(tickerSymbol: TickerSymbol): IdQuery[TickerSymbol] = {
    new IdQuery[TickerSymbol](clazz, tickerSymbol.abbreviation)
  }

  private def incrementTickProcessorThreadCount(tickerSymbol: TickerSymbol): Unit = {
    space.asyncChange(makeIdQuery(tickerSymbol), new ChangeSet().increment(tickPropName, 1))
    tickerSymbol.tickProcessorThreadCount = tickerSymbol.tickProcessorThreadCount + 1
  }

  private def incrementFeedThreadCount(tickerSymbol: TickerSymbol): Unit = {
    space.asyncChange(makeIdQuery(tickerSymbol), new ChangeSet().increment(feedPropName, 1))
    tickerSymbol.feedThreadCount = tickerSymbol.feedThreadCount + 1
  }

}