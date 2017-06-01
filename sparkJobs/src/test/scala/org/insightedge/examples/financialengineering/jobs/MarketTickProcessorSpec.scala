package org.insightedge.examples.financialengineering.jobs

import java.util.concurrent.TimeUnit

import scala.concurrent.duration._

import org.insightedge.examples.financialengineering.GigaSpaceSpec
import org.insightedge.examples.financialengineering.SpaceUsage
import org.insightedge.examples.financialengineering.model._
import org.insightedge.spark.implicits.all._
import org.openspaces.core.GigaSpace
import org.scalactic.Equality
import org.scalactic.Tolerance
import org.scalatest.BeforeAndAfterEach
import org.scalatest.FunSuite
import org.scalatest.Matchers

import com.holdenkarau.spark.testing._
import com.j_spaces.core.client.SQLQuery

class MarketTickProcessorSpec extends FunSuite with Matchers with StreamingSuiteBase with InsightedgeContextProvider with GigaSpaceSpec with InsightedgeSharedContext with BeforeAndAfterEach with SpaceUsage {

  val spaceUrl = "jini://localhost/*/test-space?groups=xap-12.0.0"
  var datagrid: GigaSpace = _

  override def beforeEach() = {
    super.beforeEach()
    datagrid = makeClusteredProxy(spaceUrl)
    datagrid.clear(null)
  }

  test("creating TickData") {
    val input = List(List("AAPL" -> MarketTick(884070480000l, 0.5166, 0.5166, 0.5166, 0.5166, 61481.0, 28.0, 0.0, 0.0),
      "AAPL" -> MarketTick(884153640000l, 0.6142, 0.6142, 0.6142, 0.6142, 107592.0, 28.0, 0.0, 0.0)))

    val expected = List(List(TickData(null, "AAPL", 884070480000l, 0.5166, 61481.0, 0.0, 0.0, 0.5166, false),
      TickData(null, "AAPL", 884153640000l, 0.6142, 107592.0, 0.0, 0.0, 0.6142, false)))

    testOperation[(String, MarketTick), TickData](input, MarketTickProcessor.createTickData _, expected, ordered = true)
  }

  test("InvestementReturns are not being created if there is no tick data for previous month") {
    val t1 = TickData(null, "AAPL", 884070480000l, 0.5166, 61481.0, 0.0, 0.0, 0.5166, false)
    val t2 = TickData(null, "AAPL", 884153640000l, 0.6142, 107592.0, 0.0, 0.0, 0.6142, false)

    val input = List(List(t1, t2))
    val expected = List(List.empty)

    testOperation[TickData, InvestmentReturn](input, MarketTickProcessor.createInvestmentReturns(spaceUrl) _, expected, ordered = true)

    val res = datagrid.readMultiple(new SQLQuery[TickData](classOf[TickData], ""))
    res.size should be(2)
    // throws MatchError if the result data is invalid
    res.map {
      case TickData(_, "AAPL", 884070480000l, 0.5166, 61481.0, 0.0, 0.0, 0.5166, true) =>
      case TickData(_, "AAPL", 884153640000l, 0.6142, 107592.0, 0.0, 0.0, 0.6142, true) =>
    }
  }

  val curT1 = TickData(null, "AAPL", 884070480000l, 0.5166, 61481.0, 0.0, 0.0, 0.5166, false)
  val curT2 = TickData(null, "AAPL", 884071440000l, 0.5205, 30740.5, 0.0, 0.0, 0.5205, false)
  val curT3 = TickData(null, "AAPL", 884153640000l, 0.6142, 107592.0, 0.0, 0.0, 0.6142, false)
  val curT4 = TickData(null, "AAPL", 884154360000l, 0.6142, 30740.5, 0.0, 0.0, 0.6142, false)

  val monthAgoTs1 = curT1.getTimestampMs - TimeUnit.DAYS.toMillis(30)

  val monthAgoAnd6minTick1 = TickData(null, "AAPL", monthAgoTs1 - TimeUnit.MINUTES.toMillis(6), 0.488, 70703.2, 0.0, 0.0, 0.488, true)
  val monthAgoAnd5minTick1 = TickData(null, "AAPL", monthAgoTs1 - TimeUnit.MINUTES.toMillis(5), 0.4392, 61481, 0.0, 0.0, 0.4392, true)
  val monthAgoAnd4minTick1 = TickData(null, "AAPL", monthAgoTs1 - TimeUnit.MINUTES.toMillis(4), 0.4392, 12296.2, 0.0, 0.0, 0.4392, true)

  val monthAgoTs2 = curT2.getTimestampMs - TimeUnit.DAYS.toMillis(30)
  val monthAgoAnd5minTick2 = TickData(null, "AAPL", monthAgoTs2 - TimeUnit.MINUTES.toMillis(5), 0.4392, 30740.5, 0.0, 0.0, 0.4392, true)
  val monthAgoAnd4minTick2 = TickData(null, "AAPL", monthAgoTs2 - TimeUnit.MINUTES.toMillis(4), 0.4434, 76851.3, 0.0, 0.0, 0.4434, true)

  val monthAgoTs3 = curT3.getTimestampMs - TimeUnit.DAYS.toMillis(30)
  val monthAgoAnd4minTick3 = TickData(null, "AAPL", monthAgoTs3 - TimeUnit.MINUTES.toMillis(4), 0.4434, 153703, 0.0, 0.0, 0.4434, true)
  val monthAgoAnd3minTick3 = TickData(null, "AAPL", monthAgoTs3 - TimeUnit.MINUTES.toMillis(3), 0.4434, 162925, 0.0, 0.0, 0.4434, true)

  val monthAgoTs4 = curT4.getTimestampMs - TimeUnit.DAYS.toMillis(30)
  val monthAgoAnd6minTick4 = TickData(null, "AAPL", monthAgoTs4 - TimeUnit.MINUTES.toMillis(6), 0.4434, 565625, 0.0, 0.0, 0.4434, true)
  val monthAgoAnd5minTick4 = TickData(null, "AAPL", monthAgoTs4 - TimeUnit.MINUTES.toMillis(5), 0.4434, 894549, 0.0, 0.0, 0.4453, true)
  val monthAgoAnd4minTick4 = TickData(null, "AAPL", monthAgoTs4 - TimeUnit.MINUTES.toMillis(4), 0.4434, 611736, 0.0, 0.0, 0.4434, true)
  val monthAgoAnd3minTick4 = TickData(null, "AAPL", monthAgoTs4 - TimeUnit.MINUTES.toMillis(3), 0.4392, 82999.4, 0.0, 0.0, 0.4434, true)

  test("creating InvestementReturns if tick data for previous month is in the gird") {
    datagrid.writeMultiple(Array(monthAgoAnd6minTick1, monthAgoAnd5minTick1, monthAgoAnd4minTick1, monthAgoAnd5minTick2, monthAgoAnd4minTick2, monthAgoAnd4minTick3, monthAgoAnd3minTick3, monthAgoAnd6minTick4, monthAgoAnd5minTick4, monthAgoAnd4minTick4, monthAgoAnd3minTick4))

    val input = List(List(curT1, curT2), List(curT3, curT4))

    val expected = List(List(InvestmentReturn(null, monthAgoTs1 - TimeUnit.MINUTES.toMillis(4), null, 6.011751683933395), InvestmentReturn(null, monthAgoTs2 - TimeUnit.MINUTES.toMillis(4), null, 5.845783917644185)),
      List(InvestmentReturn(null, monthAgoTs3 - TimeUnit.MINUTES.toMillis(3), null, 48.894371883506714), InvestmentReturn(null, monthAgoTs4 - TimeUnit.MINUTES.toMillis(3), null, 48.894371883506714)))

    testOperation[TickData, InvestmentReturn](input, MarketTickProcessor.createInvestmentReturns(spaceUrl) _, expected, ordered = true)
  }

  test("creating InvestementReturns if tick data for previous month is in the same batch") {
    val input = List(List(monthAgoAnd6minTick1, monthAgoAnd5minTick1, monthAgoAnd4minTick1, monthAgoAnd5minTick2, monthAgoAnd4minTick2, monthAgoAnd4minTick3, monthAgoAnd3minTick3, monthAgoAnd6minTick4, monthAgoAnd5minTick4, monthAgoAnd4minTick4, monthAgoAnd3minTick4, curT1, curT2), List(curT3, curT4))

    val expected = List(List(InvestmentReturn(null, monthAgoTs1 - TimeUnit.MINUTES.toMillis(4), null, 6.011751683933395), InvestmentReturn(null, monthAgoTs2 - TimeUnit.MINUTES.toMillis(4), null, 5.845783917644185)),
      List(InvestmentReturn(null, monthAgoTs3 - TimeUnit.MINUTES.toMillis(3), null, 48.894371883506714), InvestmentReturn(null, monthAgoTs4 - TimeUnit.MINUTES.toMillis(3), null, 48.894371883506714)))
    testOperation[TickData, InvestmentReturn](input, MarketTickProcessor.createInvestmentReturns(spaceUrl) _, expected, ordered = true)
  }

  test("creating MarketReturns") {
    val curTime = System.currentTimeMillis()
    val count = 2

    val ir1 = InvestmentReturn(null, curTime, "1", 0.1)
    val ir11 = InvestmentReturn(null, curTime, "2", 0.16)

    val oneSec = (1 seconds).toMillis

    val ir2 = InvestmentReturn(null, curTime + oneSec, "3", 0.2)
    val ir21 = InvestmentReturn(null, curTime + oneSec, "4", 0.32)

    val ir3 = InvestmentReturn(null, curTime + 2 * oneSec, "5", 0.3)
    val ir31 = InvestmentReturn(null, curTime + 2 * oneSec, "6", 0.2)

    val ir4 = InvestmentReturn(null, curTime + 3 * oneSec, "7", 0.4)
    val ir41 = InvestmentReturn(null, curTime + 3 * oneSec, "8", 0.35)

    val input = List(List(ir1, ir2, ir11, ir3), List(ir21, ir31, ir4, ir41))
    val expected = List(List(MarketReturn(null, curTime, 0.13, 0.0018, true)),
      List(MarketReturn(null, curTime + oneSec, 0.26, 0.0072, true), MarketReturn(null, curTime + 2 * oneSec, 0.25, 0.005, true), MarketReturn(null, curTime + 3 * oneSec, 0.375, 0.00125, true)))

    implicit val marketReturnEquality = new Equality[MarketReturn] {
      override def areEqual(a: MarketReturn, b: Any): Boolean =
        b match {
          case n: MarketReturn => {
            n.canEqual(a) && a.timestampMs == n.timestampMs && a.percentageRateOfReturn === n.percentageRateOfReturn +- .00001 && a.variance === n.variance +- .00001 && a.processed == n.processed
          }
          case _ => false
        }
    }

    val rdd = sc.emptyRDD[(Long, (Double, Double, Int))]
    testOperation[InvestmentReturn, MarketReturn](input, MarketTickProcessor.createMarketReturns(2)(rdd) _, expected, ordered = true)
  }
}