package org.insightedge.examples.financialengineering.finance

import org.scalatest.Matchers._
import org.scalatest._

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/12/17
  * Time: 9:20 PM
  */
class SimpleRegressionModelSpec extends Spec {

  private val solidsReductionPercentage = List[Double](3, 7, 11, 15, 18, 27, 29, 30, 30, 31,
    31, 32, 33, 33, 34, 36, 36, 36, 37, 38, 39, 39, 39, 40, 41, 42, 42, 43, 44, 45, 46, 47, 50)
  private val chemicalOxygenDemand = List[Double](5, 11, 21, 16, 16, 28, 27, 25, 35, 30,
    40, 32, 34, 32, 34, 37, 38, 34, 36, 38, 37, 36, 45, 39, 41, 40, 44, 37, 44, 46, 46, 49, 51)
  private val result = SimpleRegressionModel
    .leastSquares(solidsReductionPercentage zip chemicalOxygenDemand)

  object `Simple Regression Model` {

    def `should calculate a (approximation of alpha)`() {
      val estimatorA = result._1
      estimatorA should equal(3.829633 +- .000001)
    }

    def `should calculate the variance of a`(): Unit = {
      val aVariance = result._2
      aVariance should equal(3.12740 +- .0001)
    }

    def `should calculate the magnitude of the 95% confidence interval of a`(): Unit = {
      val aConfidence: Double = result._3
      aConfidence should equal(3.60763 +- .00001)
    }

    def `should calculate b (approximation of beta)`(): Unit = {
      val estimatorB = result._4
      estimatorB should equal(0.903643 +- .000001)
    }

    def `should calculate the magnitude of the 95% confidence interval of b`(): Unit = {
      val bConfidence: Double = result._6
      bConfidence should equal(.10224 +- .00001)
    }

    def `should calculate the variance of b`(): Unit = {
      val bVariance: Double = result._5
      bVariance should equal(0.0025119 +- 0.0000001) // check
    }

    def `should calculate the model variance`(): Unit = {
      val modelVariance = result._7
      modelVariance should equal(10.4299 +- .0001)
    }

  }

}