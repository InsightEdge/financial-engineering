package org.insightedge.examples.financialengineering

import org.apache.spark.SparkConf
import org.apache.spark.streaming.Milliseconds
import org.apache.spark.streaming.StreamingContext
import org.insightedge.examples.financialengineering.SparkSettings.sparkAppName
import org.insightedge.examples.financialengineering.SparkSettings.sparkCheckpointDir
import org.insightedge.examples.financialengineering.SparkSettings.sparkContextFrequencyMs
import org.insightedge.spark.context.InsightEdgeConfig
import org.insightedge.spark.implicits.all.SparkConfExtension

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 3/30/17
  * Time: 12:21 PM
  */
trait SparkUsage {

  def makeStreamingContext(appName: String = sparkAppName, frequencyInMilliseconds: Int = sparkContextFrequencyMs, checkpointDir: String = sparkCheckpointDir): StreamingContext = {
    val sparkConf: SparkConf = makeSparkConf(appName)
    val scc = new StreamingContext(sparkConf, Milliseconds(frequencyInMilliseconds))
    scc.checkpoint(checkpointDir)
    scc
  }

  private def makeSparkConf(appName: String, spaceName: String = CoreSettings.spaceName, spaceGroups: String = CoreSettings.spaceLookupGroups, spaceLocators: String = CoreSettings.spaceLookupLocators, sparkMasterUrl: String = SparkSettings.sparkMasterUrl): SparkConf = {
    val ieConfig = InsightEdgeConfig(spaceName, Some(spaceGroups), Some(spaceLocators))
    new SparkConf().setAppName(appName).setMaster(sparkMasterUrl).setInsightEdgeConfig(ieConfig)
  }
 
}
