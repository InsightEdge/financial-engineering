package org.insightedge.examples.financialengineering.jobs

import org.apache.spark.SparkContext
import org.insightedge.spark.context.InsightEdgeConfig
import org.insightedge.spark.implicits.all.SparkConfExtension

import com.holdenkarau.spark.testing.SparkContextProvider

trait InsightedgeContextProvider extends SparkContextProvider {
    
   val ieConfig = InsightEdgeConfig("test-space", Some("xap-12.0.0"), Some("localhost:4174"))
  
   override def conf = {
      val sparkConf = super.conf
      sparkConf.setInsightEdgeConfig(ieConfig)
      sparkConf
   }
}