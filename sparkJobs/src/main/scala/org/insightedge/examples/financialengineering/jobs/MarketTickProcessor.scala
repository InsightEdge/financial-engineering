package org.insightedge.examples.financialengineering.jobs

import _root_.kafka.serializer.{ Decoder, StringDecoder }

import org.apache.spark._
import org.apache.spark.rdd._
import org.apache.spark.storage._
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream._

import org.insightedge.examples.financialengineering.kafka.MarketTickDecoder
import org.insightedge.examples.financialengineering.model._
import org.insightedge.examples.financialengineering.{ KafkaSettings, CoreSettings, SparkUsage }
import org.insightedge.spark.implicits.all._
import org.insightedge.examples.financialengineering.SparkSettings._
import org.insightedge.examples.financialengineering.repos.TickerSymbols
import java.util.concurrent.TimeUnit
import InvestmentHelp._

/**
 * User: jason nerothin
 *
 * Time: 1:28 PM
 * Reads from Kafka, Writes to Data Grid.
 *
 */
object MarketTickProcessor extends App with SparkUsage {

  import org.apache.spark.streaming.kafka._

  val topics = TickerSymbols.all().map(s => (s.getAbbreviation)).toSet

  val ssc: StreamingContext = makeStreamingContext()

  val marketTickStream: DStream[(String, MarketTick)] = KafkaUtils.createDirectStream[String, MarketTick, StringDecoder, MarketTickDecoder](
    ssc,
    KafkaSettings.kafkaParams,
    topics)

  val tickStream = createTickData(marketTickStream)
  tickStream.saveToGrid()

  val investmentReturnStream = createInvestmentReturns(tickStream)
  investmentReturnStream.saveToGrid()

  val initialRDD = ssc.sparkContext.parallelize(List.empty[(Long, (Double, Double, Int))])
  val marketReturnStream = createMarketReturns(topics.size)(initialRDD)(investmentReturnStream)
  marketReturnStream.saveToGrid()

  ssc.start()
  ssc.awaitTerminationOrTimeout(TimeUnit.MINUTES.toMillis(10))
  ssc.sparkContext.stopInsightEdgeContext()

  def createTickData(marketTickStream: DStream[(String, MarketTick)]): DStream[TickData] = marketTickStream.map[TickData] { case (sym, t) => TickData(sym, t) }

  def createInvestmentReturns(tickStream: DStream[TickData]): DStream[InvestmentReturn] = {

    tickStream.transform { tickRdd =>
      val tickPairRdd = tickRdd.zipWithGridSql[TickData](s"symbol = ? AND timestampMs <= ? AND timestampMs >= ? ORDER BY timestampMs DESC", createMonthAgoTickParams, None)

      val invReturnRdd = tickPairRdd.collect { case (cur, Seq(firstRes, _*)) => Investment(cur, firstRes) }
        .map(inv => new InvestmentReturn(null, inv.getEndDateMs, inv.getId, inv.compoundAnnualGrowthRate()))

      tickRdd.map(td => { td.setProcessed(true); td }).saveToGrid()
      invReturnRdd
    }
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

  def createMonthAgoTickParams = (tick: TickData) => {
    val newTime = tick.getTimestampMs - CoreSettings.msPerMonth
    val oldTime = newTime - TimeUnit.MINUTES.toMillis(5)
    Seq(tick.getSymbol, newTime, oldTime)
  }
}