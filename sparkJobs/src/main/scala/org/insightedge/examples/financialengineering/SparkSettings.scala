package org.insightedge.examples.financialengineering

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 2/1/17
  * Time: 8:05 AM
  */
object SparkSettings {

  val ingestionFrequencyMilliseconds = 1
  val ingestionAppName = "KafkaToSpaceIngestion"
  val ingestContextFrequencyMilliseconds = 2000

  val calcIndividualFrequencyMilliseconds = 1
  val calcIndividualAppName = "CalcIndividualReturn"
  val calcIndividualContextFrequencyMilliseconds = 2000

  val calcMarketReturnFrequencyMilliseconds = 1
  val calcMarketReturnAppName = "CalcMarketReturnReturn"
  val calcMarketReturnContextFrequencyMilliseconds = 2000

  val sparkMasterUrl = "local[*]"

}
