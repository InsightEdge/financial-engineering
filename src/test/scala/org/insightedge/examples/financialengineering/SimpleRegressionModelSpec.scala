package org.insightedge.examples.financialengineering

import org.insightedge.examples.financialengineering.finance.SimpleRegressionModel
import org.scalatest.Matchers._
import org.scalatest._

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/12/17
  * Time: 9:20 PM
  */
class SimpleRegressionModelSpec extends Spec{

  private val solidsReductionPercentage = List[Double](3,  7, 11, 15, 18, 27, 29, 30, 30, 31,
    31, 32, 33, 33, 34, 36, 36, 36, 37, 38, 39, 39, 39, 40, 41, 42, 42, 43, 44, 45, 46, 47, 50)
  private val chemicalOxygenDemand =      List[Double](5, 11, 21, 16, 16, 28, 27, 25, 35, 30,
    40, 32, 34, 32, 34, 37, 38, 34, 36, 38, 37, 36, 45, 39, 41, 40, 44, 37, 44, 46, 46, 49, 51)

  object `Simple Regression Model` {

    def `should calculate alpha` {
      val result = SimpleRegressionModel.leastSquares(solidsReductionPercentage zip chemicalOxygenDemand)
      val alpha: Double = result._1
      alpha should equal(3.829633 +- .000001)
    }

    def `should calculate beta` {
      val result = SimpleRegressionModel.leastSquares(solidsReductionPercentage zip chemicalOxygenDemand)
      val beta: Double = result._2
      beta should equal(0.903643 +- .000001)
    }

    def `should do something about epsilon` = {
      val result = SimpleRegressionModel.leastSquares(solidsReductionPercentage zip chemicalOxygenDemand)
      val epsilon: Double = result._3
      epsilon should equal(0.0d)
    }

  }

}