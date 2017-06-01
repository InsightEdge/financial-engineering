package org.insightedge.examples.financialengineering.jobs

import java.util.concurrent.TimeUnit

import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.State
import org.apache.spark.streaming.StateSpec
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.dstream.DStream.toPairDStreamFunctions
import org.insightedge.examples.financialengineering.CoreSettings
import org.insightedge.examples.financialengineering.KafkaSettings
import org.insightedge.examples.financialengineering.SpaceUsage
import org.insightedge.examples.financialengineering.SparkUsage
import org.insightedge.examples.financialengineering.kafka.MarketTickDecoder
import org.insightedge.examples.financialengineering.model.Investment
import org.insightedge.examples.financialengineering.model.InvestmentHelp.Help
import org.insightedge.examples.financialengineering.model.InvestmentReturn
import org.insightedge.examples.financialengineering.model.MarketReturn
import org.insightedge.examples.financialengineering.model.MarketTick
import org.insightedge.examples.financialengineering.model.TickData
import org.insightedge.examples.financialengineering.repos.TickerSymbols
import org.insightedge.spark.implicits.all.insightEdgeSparkContext
import org.insightedge.spark.implicits.all.saveDStreamToGridExtension
import org.openspaces.core.GigaSpace

import com.j_spaces.core.client.SQLQuery

import kafka.serializer.StringDecoder
/**
 * User: jason nerothin
 *
 * Time: 1:28 PM
 * Reads from Kafka, Writes to Data Grid.
 *
 */
object MarketTickProcessor extends App with SparkUsage with SpaceUsage {

  import org.apache.spark.streaming.kafka._

  val topics = TickerSymbols.all().map(s => (s.getAbbreviation)).toSet

  val ssc: StreamingContext = makeStreamingContext()

  val marketTickStream: DStream[(String, MarketTick)] = KafkaUtils.createDirectStream[String, MarketTick, StringDecoder, MarketTickDecoder](
    ssc,
    KafkaSettings.kafkaParams,
    topics)

  val tickStream = createTickData(marketTickStream)
  tickStream.saveToGrid()

  val sc = ssc.sparkContext
  val investmentReturnStream = createInvestmentReturns()(tickStream)
  investmentReturnStream.saveToGrid()

  val initialRDD = sc.parallelize(List.empty[(Long, (Double, Double, Int))])
  val marketReturnStream = createMarketReturns(topics.size)(initialRDD)(investmentReturnStream)
  marketReturnStream.saveToGrid()

  ssc.start()
  ssc.awaitTerminationOrTimeout(TimeUnit.MINUTES.toMillis(10))
  sc.stopInsightEdgeContext()

  def createTickData(marketTickStream: DStream[(String, MarketTick)]): DStream[TickData] = marketTickStream.map[TickData] { case (sym, t) => TickData(sym, t) }

  def createInvestmentReturns(spaceUrl: String = CoreSettings.remoteJiniUrl)(tickStream: DStream[TickData]): DStream[InvestmentReturn] = {
    val invReturnStream = tickStream.mapPartitions(tick => {
      val spaceProxy = makeClusteredProxy(spaceUrl)

      val tickData = tick.toIterable

      tickData.map { cur =>
        val monthAgoTicks = tickData.collect { case other if isMonthAgoTick(cur, other) => other }

        val monthAgoTick = if (monthAgoTicks.isEmpty) {
          loadMonthAgoTick(cur, spaceProxy)
        } else {
          Some(monthAgoTicks.maxBy(_.getTimestampMs))
        }
        (cur, monthAgoTick)
      }
        .collect { case (cur, Some(old)) => Investment(cur, old) }
        .map(inv => new InvestmentReturn(null, inv.getEndDateMs, inv.getId, inv.compoundAnnualGrowthRate()))
        .toIterator
    }, false)

    tickStream.map(td => { td.setProcessed(true); td }).saveToGrid()
    invReturnStream
  }

  def createMarketReturns(symbolsCount: Int)(initialRDD: RDD[(Long, (Double, Double, Int))])(investmentReturnStream: DStream[InvestmentReturn]): DStream[MarketReturn] = {
    val calculateSumAndSquaredSum = (ts: Long, inv: Option[InvestmentReturn], state: State[(Double, Double, Int)]) => {
      val percentageRateOfReturn = inv match {
        case Some(i) => i.getPercentageRateOfReturn()
        case None => 0
      }
      val (acc, squareAcc, count) = state.getOption.getOrElse((0.0, 0.0, 0))

      val sum = percentageRateOfReturn + acc
      val squareSum = percentageRateOfReturn * percentageRateOfReturn + squareAcc

      val output = (sum, squareSum, count + 1)
      state.update(output)
      ts -> output
    }

    val sumStream = investmentReturnStream.map(i => (i.getTimestampMs, i)).mapWithState(StateSpec.function(calculateSumAndSquaredSum).initialState(initialRDD))

    sumStream.filter { case (_, (_, _, count)) => count == symbolsCount }.map {
      case (ts, (sum, squareSum, count)) =>
        val variance = (squareSum - sum * sum / count) / (count - 1)
        new MarketReturn(null, ts, sum / count, variance, true)
    }
  }

  def loadMonthAgoTick(tick: TickData, space: GigaSpace): Option[TickData] = {
    val (sym, time1, time2) = createMonthAgoTickParams(tick)
    val sqlQuery = new SQLQuery[TickData](classOf[TickData], "symbol = ? AND timestampMs <= ? AND timestampMs >= ? ORDER BY timestampMs DESC", sym.asInstanceOf[Object], time1.asInstanceOf[Object], time2.asInstanceOf[Object])
    space.readMultiple(sqlQuery).toList match {
      case x :: tail => Some(x)
      case Nil => None
    }
  }

  def isMonthAgoTick(cur: TickData, other: TickData) = {
    val (sym, time1, time2) = createMonthAgoTickParams(cur)
    other.getSymbol() == sym && other.getTimestampMs() <= time1 && other.getTimestampMs() >= time2
  }

  def createMonthAgoTickParams(tick: TickData) = {
    val monthAgo = tick.getTimestampMs - CoreSettings.msPerMonth
    val monthAgoAnd5min = monthAgo - TimeUnit.MINUTES.toMillis(5)
    (tick.getSymbol, monthAgo, monthAgoAnd5min)
  }
}