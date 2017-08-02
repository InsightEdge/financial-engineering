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
import org.apache.commons.math3.stat.regression.SimpleRegression

import java.time.{ ZonedDateTime, ZoneId, Instant }
import java.time.format.DateTimeFormatter

class MarketTickProcessorSpec extends FunSuite with Matchers with StreamingSuiteBase with InsightedgeContextProvider with GigaSpaceSpec with InsightedgeSharedContext with BeforeAndAfterEach with SpaceUsage {

  val spaceUrl = "jini://localhost/*/test-space?groups=xap-12.0.0"
  var datagrid: GigaSpace = _

  override def beforeEach() = {
    super.beforeEach()
    datagrid = makeClusteredProxy(spaceUrl)
    datagrid.clear(null)
  }

  test("creating TickData") {
    val input = List(List("AAPL" -> MarketTick(884070480000l, "1998-01-06", 0.5166, 0.5166, 0.5166, 0.5166, 61481.0, 28.0, 0.0, 0.0),
      "AAPL" -> MarketTick(884153640000l, "1998-01-06", 0.6142, 0.6142, 0.6142, 0.6142, 107592.0, 28.0, 0.0, 0.0)))

    val expected = List(List(TickData(null, "AAPL", 884070480000l, "1998-01-06", 0.5166, 61481.0, 0.0, 0.0, 0.5166, 0.5166, 0.5166),
      TickData(null, "AAPL", 884153640000l, "1998-01-06", 0.6142, 107592.0, 0.0, 0.0, 0.6142, 0.6142, 0.6142)))

    testOperation[(String, MarketTick), TickData](input, MarketTickProcessor.createTickData _, expected, ordered = true)
  }

  test("InvestementReturns are not being created if there is no tick data for previous month") {
    val t1 = TickData(null, "AAPL", 884070480000l, "1998-01-06", 0.5166, 61481.0, 0.0, 0.0, 0.5166, 0.5166, 0.5166)
    val t2 = TickData(null, "AAPL", 884153640000l, "1998-01-06", 0.6142, 107592.0, 0.0, 0.0, 0.6142, 0.6142, 0.6142)

    val input = List(List(t1, t2))
    val expected = List(List.empty)

    testOperation[TickData, InvestmentReturn](input, MarketTickProcessor.createInvestmentReturns(spaceUrl) _, expected, ordered = true)

    val res = datagrid.readMultiple(new SQLQuery[TickData](classOf[TickData], ""))
    res.size should be(0)
  }

  val curT1 = TickData(null, "AAPL", 884070480000l, "1998-01-06", 0.5166, 61481.0, 0.0, 0.0, 0.5166, 0.5166, 0.5166)
  val curT2 = TickData(null, "AAPL", 884071440000l, "1998-01-06", 0.5205, 30740.5, 0.0, 0.0, 0.5205, 0.5205, 0.5205)
  val curT3 = TickData(null, "AAPL", 884153640000l, "1998-01-07", 0.6142, 107592.0, 0.0, 0.0, 0.6142, 0.6142, 0.6142)
  val curT4 = TickData(null, "AAPL", 884154360000l, "1998-01-07", 0.6142, 30740.5, 0.0, 0.0, 0.6142, 0.6142, 0.6142)

  val monthAgoTs1 = curT1.getTimestampMs - TimeUnit.DAYS.toMillis(30)

  val monthAgoAnd6minTick1 = TickData(null, "AAPL", monthAgoTs1 - TimeUnit.MINUTES.toMillis(6), "1998-01-06", 0.488, 70703.2, 0.0, 0.0, 0.488, 0.488, 0.488)
  val monthAgoAnd5minTick1 = TickData(null, "AAPL", monthAgoTs1 - TimeUnit.MINUTES.toMillis(5), "1998-01-06", 0.4392, 61481, 0.0, 0.0, 0.4392, 0.4392, 0.4392)
  val monthAgoAnd4minTick1 = TickData(null, "AAPL", monthAgoTs1 - TimeUnit.MINUTES.toMillis(4), "1998-01-06", 0.4392, 12296.2, 0.0, 0.0, 0.4392, 0.4392, 0.4392)

  val monthAgoTs2 = curT2.getTimestampMs - TimeUnit.DAYS.toMillis(30)
  val monthAgoAnd5minTick2 = TickData(null, "AAPL", monthAgoTs2 - TimeUnit.MINUTES.toMillis(5), "1998-01-06", 0.4392, 30740.5, 0.0, 0.0, 0.4392, 0.4392, 0.4392)
  val monthAgoAnd4minTick2 = TickData(null, "AAPL", monthAgoTs2 - TimeUnit.MINUTES.toMillis(4), "1998-01-06", 0.4434, 76851.3, 0.0, 0.0, 0.4434, 0.4434, 0.4434)

  val monthAgoTs3 = curT3.getTimestampMs - TimeUnit.DAYS.toMillis(30)
  val monthAgoAnd4minTick3 = TickData(null, "AAPL", monthAgoTs3 - TimeUnit.MINUTES.toMillis(4), "1998-01-07", 0.4434, 153703, 0.0, 0.0, 0.4434, 0.4434, 0.4434)
  val monthAgoAnd3minTick3 = TickData(null, "AAPL", monthAgoTs3 - TimeUnit.MINUTES.toMillis(3), "1998-01-07", 0.4434, 162925, 0.0, 0.0, 0.4434, 0.4434, 0.4434)

  val monthAgoTs4 = curT4.getTimestampMs - TimeUnit.DAYS.toMillis(30)
  val monthAgoAnd6minTick4 = TickData(null, "AAPL", monthAgoTs4 - TimeUnit.MINUTES.toMillis(6), "1998-01-07", 0.4434, 565625, 0.0, 0.0, 0.4434, 0.4434, 0.4434)
  val monthAgoAnd5minTick4 = TickData(null, "AAPL", monthAgoTs4 - TimeUnit.MINUTES.toMillis(5), "1998-01-07", 0.4434, 894549, 0.0, 0.0, 0.4453, 0.4453, 0.4453)
  val monthAgoAnd4minTick4 = TickData(null, "AAPL", monthAgoTs4 - TimeUnit.MINUTES.toMillis(4), "1998-01-07", 0.4434, 611736, 0.0, 0.0, 0.4434, 0.4434, 0.4434)
  val monthAgoAnd3minTick4 = TickData(null, "AAPL", monthAgoTs4 - TimeUnit.MINUTES.toMillis(3), "1998-01-07", 0.4392, 82999.4, 0.0, 0.0, 0.4434, 0.4434, 0.4434)

  implicit val equality = new Equality[InvestmentReturn] {
      override def areEqual(a: InvestmentReturn, b: Any): Boolean =
        b match {
          case n: InvestmentReturn => {
            n.canEqual(a) && a.tickerSymbol == n.tickerSymbol && a.percentageRateOfReturn === n.percentageRateOfReturn +- .0001 && a.timestampMs == n.timestampMs && a.dateAsStr == n.dateAsStr
          }
          case _ => false
        }
    }
  
  test("creating InvestementReturns if tick data for previous month is in the gird") {
    datagrid.writeMultiple(Array(monthAgoAnd6minTick1, monthAgoAnd5minTick1, monthAgoAnd4minTick1, monthAgoAnd5minTick2, monthAgoAnd4minTick2, monthAgoAnd4minTick3, monthAgoAnd3minTick3, monthAgoAnd6minTick4, monthAgoAnd5minTick4, monthAgoAnd4minTick4, monthAgoAnd3minTick4))

    val input = List(List(curT1, curT2), List(curT3, curT4))

    val expectedInvestmentReturns = List(List(InvestmentReturn(null, curT1.getTimestampMs, "1998-01-06", "AAPL", 6.0117), 
        InvestmentReturn(null, curT2.getTimestampMs, "1998-01-06", "AAPL", 5.8457)),
      List(InvestmentReturn(null, curT3.getTimestampMs, "1998-01-07", "AAPL", 48.8943), 
          InvestmentReturn(null, curT4.getTimestampMs, "1998-01-07", "AAPL", 54.9304)))

    testOperation[TickData, InvestmentReturn](input, MarketTickProcessor.createInvestmentReturns(spaceUrl) _, expectedInvestmentReturns, ordered = true)
  }

  test("creating InvestementReturns if tick data for previous month is in the same batch") {
    val input = List(List(monthAgoAnd6minTick1, monthAgoAnd5minTick1, monthAgoAnd4minTick1, curT1), List(monthAgoAnd5minTick2, monthAgoAnd4minTick2, curT2), List(monthAgoAnd4minTick3, monthAgoAnd3minTick3, curT3), List(monthAgoAnd6minTick4, monthAgoAnd5minTick4, monthAgoAnd4minTick4, monthAgoAnd3minTick4, curT4))

    val expectedInvestmentReturns = List(List(InvestmentReturn(null, curT1.getTimestampMs, "1998-01-06", "AAPL", 6.0117)), 
      List(InvestmentReturn(null, curT2.getTimestampMs, "1998-01-06", "AAPL", 5.8457)),
      List(InvestmentReturn(null, curT3.getTimestampMs, "1998-01-07", "AAPL", 48.8943)), 
      List(InvestmentReturn(null, curT4.getTimestampMs, "1998-01-07", "AAPL", 54.9304)))
        
    testOperation[TickData, InvestmentReturn](input, MarketTickProcessor.createInvestmentReturns(spaceUrl) _, expectedInvestmentReturns, ordered = true)
  }

  test("creating MarketReturns") {
    val curTime = System.currentTimeMillis()
    val curDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(ZonedDateTime.now())
    val count = 2

    val ir1 = InvestmentReturn(null, curTime, curDate, "TEST1", 0.1)
    val ir11 = InvestmentReturn(null, curTime, curDate, "TEST2", 0.16)

    val oneSec = (1 seconds).toMillis

    val ir2 = InvestmentReturn(null, curTime + oneSec, curDate, "TEST1", 0.2)
    val ir21 = InvestmentReturn(null, curTime + oneSec, curDate, "TEST2", 0.32)

    val ir3 = InvestmentReturn(null, curTime + 2 * oneSec, curDate, "TEST1", 0.3)
    val ir31 = InvestmentReturn(null, curTime + 2 * oneSec, curDate, "TEST2", 0.2)

    val ir4 = InvestmentReturn(null, curTime + 3 * oneSec, curDate, "TEST1", 0.4)
    val ir41 = InvestmentReturn(null, curTime + 3 * oneSec, curDate, "TEST2", 0.35)

    val input = List(List(ir1, ir2, ir11, ir3), List(ir21, ir31, ir4, ir41))
    val expected = List(List(MarketReturn(null, curTime, curDate, 0.13, 0.0018)),
      List(MarketReturn(null, curTime + oneSec, curDate, 0.26, 0.0072), MarketReturn(null, curTime + 2 * oneSec, curDate, 0.25, 0.005), MarketReturn(null, curTime + 3 * oneSec, curDate, 0.375, 0.00125)))

    implicit val marketReturnEquality = new Equality[MarketReturn] {
      override def areEqual(a: MarketReturn, b: Any): Boolean =
        b match {
          case n: MarketReturn => {
            n.canEqual(a) && a.timestampMs == n.timestampMs && a.percentageRateOfReturn === n.percentageRateOfReturn +- .00001 && a.variance === n.variance +- .00001 && a.dateAsStr == n.dateAsStr
          }
          case _ => false
        }
    }

    val rdd = sc.emptyRDD[(Long, (Double, Double, Int))]
    testOperation[InvestmentReturn, MarketReturn](input, MarketTickProcessor.createMarketReturns(2, rdd) _, expected, ordered = true)
  }
  
  test("creating CharacteristicLine") {
      val curTime = System.currentTimeMillis()
      val curDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(ZonedDateTime.now())
      val oneSec = (1 seconds).toMillis
      
      val ir11 = InvestmentReturn(null, curTime, curDate, "TEST1", 0.1)
      val ir21 = InvestmentReturn(null, curTime + oneSec, curDate, "TEST1", 0.2)
      val ir31 = InvestmentReturn(null, curTime + 2 * oneSec, curDate, "TEST1", 0.32)
      
      val ir12 = InvestmentReturn(null, curTime, curDate, "TEST2", 0.32)
      val ir22 = InvestmentReturn(null, curTime + oneSec, curDate, "TEST2", 0.35)
      val ir32 = InvestmentReturn(null, curTime + 2 * oneSec, curDate, "TEST2", 0.4)
      
      datagrid.writeMultiple(Array(ir11, ir12, ir21, ir22, ir31, ir32))
      
      val mr1 = MarketReturn(null, curTime, curDate, 0.21, 0.11)
      val mr2 = MarketReturn(null, curTime + oneSec, curDate, 0.275, 0.075)
      val mr3 = MarketReturn(null, curTime + 2 * oneSec, curDate, 0.36, 0.0032)

      val input = List(List(mr1, mr2, mr3))
      
      // calculated in Excel
      val expected = List(List(CharacteristicLine(null, curTime + 2 * oneSec, curDate, "TEST1", -0.20567, 0.010295, 0.130815, 1.463918, 0.035712, 0.453769, 0.003799), 
          CharacteristicLine(null, curTime + 2 * oneSec, curDate, "TEST2", 0.20567, 0.010295, 0.130815, 0.536082, 0.035712, 0.453769, 0.003799)))
      
      implicit val clEquality = new Equality[CharacteristicLine] {
      override def areEqual(a: CharacteristicLine, b: Any): Boolean =
        b match {
          case n: CharacteristicLine => {
            n.canEqual(a) && a.timestampMs == n.timestampMs && a.a === n.a +- .00001 && a.aConfidenceIntervalMagnitude === n.aConfidenceIntervalMagnitude +- .00001 && a.aVariance === n.aVariance +- .00001 && a.b === n.b +- .00001 && a.bConfidenceIntervalMagnitude === n.bConfidenceIntervalMagnitude +- .00001 && a.bVariance === n.bVariance +- .00001 && a.dateAsStr == n.dateAsStr
          }
          case _ => false
        }
     }

      val rdd = sc.emptyRDD[(String, SimpleRegression)]
      testOperation[MarketReturn, CharacteristicLine](input, MarketTickProcessor.calculateAlphaAndBetas(3, rdd) _, expected, ordered = false)
  }
}