package org.insightedge.examples.financialengineering.finance

import org.insightedge.examples.financialengineering.model.{RegressionResult, Stock, Trade}
import org.insightedge.scala.annotation.SpaceId

import scala.beans.BeanProperty

/**
  * User: jason
  *
  * Time: 3:29 PM
  *
  * The Security Characteristic Line (SCL) for the `i-th` security
  * of securities `N = (1,2,...i...,n)` at time t is given by:
  *
  * `R,,,i,t,,, - R,,,f,t,,, = alpha,,,i,,, + beta,,,i,,,(R,,,m,t,,, - R,,,f,t,,,) + epsilon,,,i,t,,,`
  *
  * where:
  *
  * `R,,,i,t,,,` = return for the i-th asset
  * `R,,,f,t,,,` = return of a risk-free asset
  * `R,,,m,t,,,` = return on "the market"
  *
  * `epsilon i,t` = Random error
  *
  * Assumptions about epsilon:
  * (i)   `E(epsilon,,,i,t,,,) = 0` for all t,
  *          i.e. error has zero mean for all time periods
  * (ii)  `E(epsilon^2^,,,i,t,,,) = sigma^2^,,,i,,,` for all t,
  *          i.e. Error has constant variance for all time periods
  * (iii) `E(epsilon,,,i,s,,,epsilon,,,i,t,,,) = 0` for all s that are not t,
  *          i.e. errors are uncorrelated to each other at different times
  *
  * We use a monthly term in our calculations, but there seems to be no
  * reason it could not be generalized.
  *
  * @see https://en.wikipedia.org/wiki/Security_characteristic_line
  * @see http://faculty.smu.edu/tfomby/eco5350/data/Lecture_Notes/Least%20Squares%20and%20the%20SCL.pdf
  */
class SecurityCharacteristics {

  import org.insightedge.examples.financialengineering.model.TradeHelp._

  /**
    * `R,,,t,,, = (P,,,t,,, - P,,,t-1,,, + d,,,t,,,) / P,,,t-1,,,`
    *
    * where
    *
    * `R,,,t,,, = monthly rate of return on the investment at end of time period t`
    * `P,,,t,,, = price of investment at end of time period t`
    * `P,,,t-1,,, = price of investment at beginning of time period t`
    * `d,,,t,,, is the dividend paid during the period, if any`
    *
    * @param trade subject of this calculation
    * @return a return ;)
    */
  def monthlyRateOfReturn(trade: Trade): Double = {
    val Pt = trade.sellPrice
    val Ptm1 = trade.buyPrice
    val div = trade.dividendsDuringTerm
    val totalReturn = Pt - Ptm1 + div
    val monthlyReturn = totalReturn / trade.months // ?
    monthlyReturn
  }

  /** @param portfolio [Stock][Trade]s
    * @return a list of monthly market returns, weighted by market capitalization
    */
  def weightedMonthlyReturns(portfolio: List[(Stock, Trade)]): List[Double] = {
    def weight(s: Stock, t: Trade): Double = {
      monthlyRateOfReturn(t) / s.outstandingShares
    }

    portfolio.map(st => weight(st._1, st._2))
  }

  /** @return the average return for the given portfolio. Returns
    *         are weighted by the market capitalization of an [[Trade]]'s [[Stock]].
    * @param portfolio some [[Stock]]-based [[Trade]]s
    */
  def marketReturn(portfolio: List[(Stock, Trade)]): Double = {
    val returns = weightedMonthlyReturns(portfolio)
    (0.0 /: returns) {
      _ + _
    } / returns.length
  }

  def simpleRegressionModel(): RegressionResult = {
    null
  }

}

object SecurityCharacteristics extends SecurityCharacteristics