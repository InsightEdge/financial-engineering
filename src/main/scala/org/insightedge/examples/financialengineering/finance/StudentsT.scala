package org.insightedge.examples.financialengineering.finance

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/21/17
  * Time: 9:37 PM
  */
trait StudentsT {

  def tVal(degreesFreedom: Short, confidence: Double): Double = {
    require(degreesFreedom > 0 && degreesFreedom <= 40, "degreesOfFreedom must be <= 40")
    val ninetyFive = List(
      6.314D, 2.920D, 2.353D, 2.132D, 2.015D,
      1.943D, 1.895D, 1.860D, 1.833D, 1.812D,
      1.796D, 1.782D, 1.771D, 1.761D, 1.753D,
      1.746D, 1.740D, 1.734D, 1.729D, 1.725D,
      1.721D, 1.717D, 1.714D, 1.711D, 1.708D,
      1.706D, 1.703D, 1.701D, 1.699D, 1.697D
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