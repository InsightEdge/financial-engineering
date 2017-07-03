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
import org.insightedge.examples.financialengineering.model.InvestmentHelp.Help
import org.insightedge.examples.financialengineering.model._
import org.insightedge.examples.financialengineering.repos.TickerSymbols
import org.apache.commons.math3.stat.regression.SimpleRegression
import org.apache.commons.math3.distribution.TDistribution

import org.insightedge.spark.implicits.all._

import org.openspaces.core.GigaSpace

import com.j_spaces.core.client.SQLQuery

import kafka.serializer.StringDecoder
import java.time.{ ZonedDateTime, ZoneId, Instant }

/**
 * User: jason nerothin
 *
 * Time: 1:28 PM
 * Reads from Kafka, Writes to Data Grid.
 *
 */
object MarketTickProcessor extends App with SparkUsage with SpaceUsage {

  import org.apache.spark.streaming.kafka._

  val ssc: StreamingContext = makeStreamingContext()

  val marketTickStream: DStream[(String, MarketTick)] = KafkaUtils.createDirectStream[String, MarketTick, StringDecoder, MarketTickDecoder](
    ssc,
    KafkaSettings.kafkaParams,
    Set(KafkaSettings.kafkaTopic))

  val tickStream = createTickData(marketTickStream)
  tickStream.cache()
  tickStream.saveToGrid()

  val sc = ssc.sparkContext
  val investmentReturnStream = createInvestmentReturns()(tickStream)
  investmentReturnStream.cache()
  investmentReturnStream.saveToGrid()

  val symbolCount = TickerSymbols.count()

  val mrInitialRDD = sc.parallelize(List.empty[(Long, (Double, Double, Int))])
  val marketReturnStream = createMarketReturns(symbolCount, mrInitialRDD)(investmentReturnStream)
  marketReturnStream.cache()
  marketReturnStream.saveToGrid()

  val clInitialRDD = sc.parallelize(List.empty[(String, SimpleRegression)])
  val characteristicLineStream = calculateAlphaAndBetas(CoreSettings.characterLineFrequency, clInitialRDD)(marketReturnStream)
  characteristicLineStream.saveToGrid()

  ssc.start()
  ssc.awaitTermination()
  sc.stopInsightEdgeContext()

  def createTickData(marketTickStream: DStream[(String, MarketTick)]): DStream[TickData] = marketTickStream.map[TickData] { case (sym, t) => TickData(sym, t) }

  def createInvestmentReturns(spaceUrl: String = CoreSettings.remoteJiniUrl)(tickStream: DStream[TickData]): DStream[InvestmentReturn] = {
    tickStream.mapPartitions(tick => {
      val spaceProxy = makeClusteredProxy(spaceUrl)

      val tickData = tick.toIterable

      tickData.map { cur =>
        val monthAgoTicks = tickData.collect { case other if isMonthAgoTick(cur, other) => other }

        val monthAgoTick = if (monthAgoTicks.isEmpty) {
          loadMonthAgoTick(cur, spaceProxy)
        } else {
          Some(monthAgoTicks.maxBy(_.getTimestampMs))
        }
        cur -> monthAgoTick
      }
        .collect { case (cur, Some(old)) => (cur.getSymbol(), Investment(cur, old)) }
        .map { case (sym, inv) => new InvestmentReturn(null, inv.endDateMs, sym, inv.compoundAnnualGrowthRate()) }
        .toIterator
    }, false)
  }

  def createMarketReturns(symbolsCount: Int, initialRDD: RDD[(Long, (Double, Double, Int))])(investmentReturnStream: DStream[InvestmentReturn]): DStream[MarketReturn] = {
    val calculate = (ts: Long, inv: Option[Double], state: State[(Double, Double, Int)]) => {

      val percentageRateOfReturn = inv.getOrElse(0d)
      val (acc, squareAcc, count) = state.getOption.getOrElse((0.0, 0.0, 0))

      val sum = percentageRateOfReturn + acc
      val squareSum = percentageRateOfReturn * percentageRateOfReturn + squareAcc
      val newCount = count + 1

      if (newCount < symbolsCount) {
        state.update((sum, squareSum, newCount))
        None
      } else {
        state.remove()
        val mean = sum / newCount
        val variance = (squareSum - sum * sum / newCount) / count
        Some(new MarketReturn(null, ts, mean, variance, false))
      }
    }: Option[MarketReturn]

    investmentReturnStream.map(i => (i.getTimestampMs, i.getPercentageRateOfReturn))
      .mapWithState(StateSpec.function(calculate).initialState(initialRDD))
      .transform(rdd => rdd.collect { case Some(marketReturn) => marketReturn })
  }

  def calculateAlphaAndBetas(frequency: Long, initialRDD: RDD[(String, SimpleRegression)])(marketReturnStream: DStream[MarketReturn]): DStream[CharacteristicLine] = {
    val accumulateRegressors = (tickerSymbol: String, pair: Option[(MarketReturn, InvestmentReturn)], state: State[SimpleRegression]) => {
      val simpleRegression = state.getOption.getOrElse(new SimpleRegression(true))
      if (pair.isDefined) {
        val (mr, ir) = pair.get
        simpleRegression.addData(mr.getPercentageRateOfReturn, ir.getPercentageRateOfReturn)
        val n = simpleRegression.getN
        if (n < frequency) {
          state.update(simpleRegression)
          None
        } else {
          state.remove()

          val distribution = new TDistribution(n - 2L)
          val aStdErr = simpleRegression.getInterceptStdErr
          val aConfidenceInterval = distribution.inverseCumulativeProbability(1 - CoreSettings.confidenceIntervalAlpha / 2.0D) * aStdErr

          Some(new CharacteristicLine(
            id = null,
            timestampMs = mr.getTimestampMs(),
            tickerSymbol = tickerSymbol,
            a = simpleRegression.getIntercept,
            aVariance = aStdErr,
            aConfidenceIntervalMagnitude = aConfidenceInterval,
            b = simpleRegression.getSlope,
            bConfidenceIntervalMagnitude = simpleRegression.getSlopeConfidenceInterval(CoreSettings.confidenceIntervalAlpha),
            bVariance = simpleRegression.getSlopeStdErr,
            modelErrorVariance = Math.sqrt(simpleRegression.getMeanSquareError)))
        }
      } else {
        None
      }
    }: Option[CharacteristicLine]

    marketReturnStream.transform(mrRdd => {
      mrRdd.zipWithGridSql[InvestmentReturn]("timestampMs = ?", (marketReturn: MarketReturn) => Seq(marketReturn.getTimestampMs.asInstanceOf[Object]), None)
        .flatMap {
          case (mr, irSeq) =>
            irSeq.groupBy(_.getTickerSymbol).flatMap { case (sym, invReturnSeq) => invReturnSeq.map(ir => (sym, (mr, ir))) }
        }
    })
      .mapWithState(StateSpec.function(accumulateRegressors).initialState(initialRDD))
      .transform(rdd => rdd.collect { case Some(cl) => cl })
  }

  def loadMonthAgoTick(tick: TickData, space: GigaSpace): Option[TickData] = {
    val (sym, time1, time2) = createMonthAgoTickParams(tick)
    val sqlQuery = new SQLQuery[TickData](classOf[TickData], "symbol = ? AND timestampMs <= ? AND timestampMs >= ? ORDER BY timestampMs DESC", sym.asInstanceOf[Object], time1.asInstanceOf[Object], time2.asInstanceOf[Object])
    space.readMultiple(sqlQuery).toList.headOption
  }

  def isMonthAgoTick(cur: TickData, other: TickData) = {
    val (sym, time1, time2) = createMonthAgoTickParams(cur)
    other.getSymbol() == sym && other.getTimestampMs() <= time1 && other.getTimestampMs() >= time2
  }

  def createMonthAgoTickParams(tick: TickData) = {
    val time = tick.getTimestampMs - CoreSettings.msPerMonth
    (tick.getSymbol, time, time - CoreSettings.ticksWindowMs)
  }
}