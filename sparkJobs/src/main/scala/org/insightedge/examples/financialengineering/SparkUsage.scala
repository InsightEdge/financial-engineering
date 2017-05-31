package org.insightedge.examples.financialengineering

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import org.insightedge.spark.context.InsightEdgeConfig

import org.insightedge.spark.implicits.all._
import org.insightedge.examples.financialengineering.SparkSettings._
/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 3/30/17
  * Time: 12:21 PM
  */
trait SparkUsage {

  def makeStreamingContext(sparkAppName: String = sparkAppName, frequencyInMilliseconds: Int = sparkContextFrequencyMs, checkpointDir: String = sparkCheckpointDir): StreamingContext = {
    val sparkConf: SparkConf = makeSparkConf(appName = sparkAppName)
    val scc = new StreamingContext(sparkConf, Milliseconds(frequencyInMilliseconds))
    scc.checkpoint(checkpointDir)
    scc
  }

  def makeSqlContext(spaceName: String, spaceGroups: String, spaceLocators: String, sparkMasterUrl: String, appName: String): SQLContext = {
    val sparkConf = makeSparkConf(spaceName, spaceGroups, spaceLocators, sparkMasterUrl)
    val sc = new SparkContext(sparkConf)
    new SQLContext(sc)
  }

  private def makeSparkConf(appName: String, spaceName: String = CoreSettings.spaceName, spaceGroups: String = CoreSettings.spaceLookupGroups, spaceLocators: String = CoreSettings.spaceLookupLocators, sparkMasterUrl: String = SparkSettings.sparkMasterUrl): SparkConf = {
    val ieConfig = InsightEdgeConfig(spaceName, Some(spaceGroups), Some(spaceLocators))
    new SparkConf().setAppName(appName).setMaster(sparkMasterUrl).setInsightEdgeConfig(ieConfig)
  }

}
