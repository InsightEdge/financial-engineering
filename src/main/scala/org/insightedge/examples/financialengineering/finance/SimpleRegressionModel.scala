package org.insightedge.examples.financialengineering.finance

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/12/17
  * Time: 7:40 PM
  *
  */
class SimpleRegressionModel {

  /**
    * This method comes from wikipedia:
    * https://en.wikipedia.org/wiki/Ordinary_least_squares#Simple_regression_model
    *
    * @param xsAndYs x-values and y-values
    * @return alpha, beta and epsilon
    */
  def leastSquares(xsAndYs: List[(Double, Double)]): (Double, Double, Double) = {
    def b(xsAndYs: List[(Double, Double)]): Double = {
      0
    }
    def a(xsAndYs: List[(Double, Double)], b: Double): Double = {
      val  n = xsAndYs.size
      val sumsOfXandY = xsAndYs.foldLeft(0d)(((o:Double,t:Double) => o + t))
    }

    def epsilon(xsAndYs: List[(Double, Double)]): Double = {
      0
    }

    val b = b(xsAndYs)
    val a = a(xsAndYs, b)

    (a, b, epsilon(xsAndYs))
  }

}

object SimpleRegressionModel extends SimpleRegressionModel
