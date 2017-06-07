package org.insightedge.examples.financialengineering

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 2/1/17
  * Time: 8:05 AM
  */
object SparkSettings {

  val sparkAppName = "CalcIndividualAndMarketReturn"
  val sparkContextFrequencyMs = 2000

  val sparkMasterUrl = "spark://127.0.0.1:7077"
  val sparkCheckpointDir = "/tmp"

}
