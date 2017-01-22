package org.insightedge.examples.financialengineering.finance

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/12/17
  * Time: 7:40 PM
  *
  */
class SimpleRegressionModel {

  private val confidenceLevel = 0.95D

  /**
    * This method comes from wikipedia:
    * https://en.wikipedia.org/wiki/Ordinary_least_squares#Simple_regression_model
    *
    * @param xsAndYs list of regressors as (x,y)
    * @return ( a - estimator of alpha, variance of a, confidence interval magnitude of a,
    *         b - estimator of beta, variance of a, confidence interval magnitude of b,
    *         modelVariance )
    */
  def leastSquares(xsAndYs: Seq[(Double, Double)]): (Double, Double, Double, Double, Double, Double, Double) = {
    val z = 0d

    def bAndOtherStuff(xsAndYs: List[(Double, Double)]): (Double, Double, Double, Double, Double, Double, Double) = {
      val n = xsAndYs.length.toDouble
      var sumOfProducts = z
      var sumOfXs = z
      var sumOfYs = z
      var sumOfXSquared = z
      var sumOfYSquared = z
      for (xy <- xsAndYs) {
        val x = xy._1
        val y = xy._2
        sumOfXs = sumOfXs + x
        sumOfYs = sumOfYs + y
        sumOfProducts = sumOfProducts + x * y
        sumOfXSquared = sumOfXSquared + x * x
        sumOfYSquared = sumOfYSquared + y * y
      }

      val bNum = n * sumOfProducts - sumOfXs * sumOfYs
      val bDen = n * sumOfXSquared - sumOfXs * sumOfXs
      val b = bNum / bDen

      val xMean = sumOfXs / n
      val yMean = sumOfYs / n

      var Syy = z
      var Sxy = z
      var Sxx = z
      for (xy <- xsAndYs) {
        val x = xy._1
        val y = xy._2
        val xDiff = x - xMean
        val yDiff = y - yMean
        Sxx = Sxx + xDiff * xDiff
        Sxy = Sxy + xDiff * yDiff
        Syy = Syy + yDiff * yDiff
      }
      val modelVariance = math.abs((Syy - b * Sxy) / n - 2)

      (b, sumOfXs, sumOfYs, sumOfXSquared, sumOfYSquared, modelVariance, Sxx)
    }

    def a(xsAndYs: List[(Double, Double)], sumOfXs: Double, sumOfYs: Double, b: Double): Double = {
      (sumOfYs - b * sumOfXs) / xsAndYs.length
    }

    val n = xsAndYs.length

    val (bValue, sumX, sumY, sumOfXSquared, sumOfYSquared, modelVariance, sxx) = bAndOtherStuff(xsAndYs)
    val sigmaEstimate = math.sqrt(modelVariance) // a.k.a. "s"
    val bVariance = modelVariance / sumOfXSquared
    val tAlpha2 = StudentsT.tVal(n.toShort, confidenceLevel)
    val bConfidenceInterval = tAlpha2 * sigmaEstimate / math.sqrt(sxx)

    val aValue = a(xsAndYs, sumX, sumY, bValue)
    val aVariance = sumOfXSquared * modelVariance / n * sxx
    val aConfidenceInterval = tAlpha2 * math.sqrt(sumOfXSquared) / math.sqrt(n * sxx)

    (aValue, aVariance, aConfidenceInterval, bValue, bVariance, bConfidenceInterval, modelVariance)
  }

}

object SimpleRegressionModel extends SimpleRegressionModel