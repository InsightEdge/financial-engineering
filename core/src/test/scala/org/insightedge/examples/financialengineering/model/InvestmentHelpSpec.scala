package org.insightedge.examples.financialengineering.model


import org.scalatest.Matchers._
import org.insightedge.examples.financialengineering.CoreSettings
import org.scalatest.Spec

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/20/17
  * Time: 4:32 PM
  */
class InvestmentHelpSpec extends Spec {

  private val nowMs = System.currentTimeMillis
  private val laterMs = nowMs + CoreSettings.msPerDay
  private val msPerYear = CoreSettings.msPerDay * CoreSettings.daysPerYear

  private def testInvestment: Investment = {
    Investment("id",
      buyPrice = 1,
      sellPrice = 2,
      startDateMs = nowMs,
      endDateMs = laterMs,
      sellCost = 3,
      buyCost = 2,
      dividendsDuringTerm = .5d
    )
  }

  import InvestmentHelp.Help

  object `Investment Help (implicit class)` {

    object `investment interval` {

      def `should be calculated in years`(): Unit = {
        testInvestment.years should equal((1D / CoreSettings.daysPerYear) +- .001)
      }

      def `should be calculated as a java.time.Duration`(): Unit = {
        testInvestment.duration().getSeconds should equal(60 * 24)
      }
    }

    object `should calculate CAGR` {
      def `when there are no costs or dividends`(): Unit = {
        val inv = testInvestment
        inv.buyCost = 0
        inv.sellCost = 0
        inv.dividendsDuringTerm = 0
        val result = inv.CAGR()
        val expected = math.pow((2 + 0 - 0 - 0) / 1, 1 / inv.years) - 1
        result should equal(expected +- .0001)
      }

      def `when there is a cost to buy`(): Unit = {
        val inv = testInvestment
        inv.buyCost = 1
        inv.sellCost = 0
        inv.dividendsDuringTerm = 0
        val result = inv.CAGR()
        val expected = math.pow((2 + 0 - 1 - 0) / 1, 1 / inv.years) - 1
        result should equal(expected +- .0001)
      }

      def `when there is a cost to sell`(): Unit = {
        val inv = testInvestment
        inv.buyCost = 0
        inv.sellCost = 1
        inv.dividendsDuringTerm = 0
        val result = inv.CAGR()
        val expected = math.pow((2 + 0 - 0 - 1) / 1, 1 / inv.years) - 1
        result should equal(expected +- .0001)
      }

      def `when there are dividends paid during the term of the investment`(): Unit = {
        val inv = testInvestment
        inv.dividendsDuringTerm = 1
        inv.buyCost = 0
        inv.sellCost = 0
        val expected = math.pow((2 - 0 - 0 + 1) / 1, 1 / inv.years)
        val result = inv.CAGR()
        result should equal(expected +- .0001)
      }
    }

  }

}