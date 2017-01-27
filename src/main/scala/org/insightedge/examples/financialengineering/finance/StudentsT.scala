package org.insightedge.examples.financialengineering.finance

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/21/17
  * Time: 9:37 PM
  */
trait StudentsT {

  def tVal(degreesFreedom: Int, confidence: Double): Double = {
    require(degreesFreedom > 0 && degreesFreedom <= 40, "degreesOfFreedom must be <= 40")
    val ninetyFive = List(
      1.706D, 4.303D, 3.182D, 2.776D, 2.571D,
      2.447D, 2.365D, 2.306D, 2.262D, 2.228D,
      2.201D, 2.179D, 2.160D, 2.145D, 2.131D,
      2.120D, 2.110D, 2.101D, 2.093D, 2.086D,
      2.080D, 2.074D, 2.069D, 2.064D, 2.060D,
      2.056D, 2.052D, 2.048D, 2.045D, 2.042D,
      2.040D, 2.037D, 2.035D, 2.032D, 2.030D,
      2.028D, 2.026D, 2.024D, 2.023D, 2.021D
    )
    confidence match {
      case 0.95d =>
        ninetyFive(degreesFreedom)
      case _ =>
        throw new IllegalArgumentException("We don't support anything except 95% confidence intervals for degrees of freedom <=- 40 at this time.")
    }
  }

}

object StudentsT extends StudentsT