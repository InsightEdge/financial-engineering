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
    val z = 0d

    def bSumOfXSumOfY(xsAndYs: List[(Double, Double)]): (Double, Double, Double) = {
      val n = xsAndYs.length.toDouble
      val sumOfProducts = xsAndYs.foldLeft(z) { (a, i: (Double, Double)) => a + i._1 * i._2 }
      val sumOfXs = xsAndYs.foldLeft(z) { (a, i: (Double, Double)) => a + i._1 }
      val sumOfYs = xsAndYs.foldLeft(z) { (a, i: (Double, Double)) => a + i._2 }
      val numerator = sumOfProducts - sumOfXs * sumOfYs / n
      val sumOfXsquared = xsAndYs.foldLeft(z) { (a, i: (Double, Double)) => a + i._1 * i._1 }
      val demnominator = sumOfXsquared - sumOfXs * sumOfXs / n
      (numerator / demnominator, sumOfXs, sumOfYs)
    }

    def a(xsAndYs: List[(Double, Double)], sumOfXs: Double, sumOfYs: Double, b: Double): Double = {
      (sumOfYs - b * sumOfXs) / xsAndYs.length
    }

    def epsilon(xsAndYs: List[(Double, Double)]): Double = {
      0
    }

    val (bVal, sumX, sumY) = bSumOfXSumOfY(xsAndYs)
    val aVal: Double = a(xsAndYs, sumX, sumY, bVal)

    (aVal, bVal, epsilon(xsAndYs))
  }

}

object SimpleRegressionModel extends SimpleRegressionModel
