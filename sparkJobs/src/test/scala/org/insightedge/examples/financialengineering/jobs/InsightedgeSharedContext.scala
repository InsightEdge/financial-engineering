package org.insightedge.examples.financialengineering.jobs

import org.insightedge.examples.financialengineering.GigaSpaceSpec
import com.holdenkarau.spark.testing.SharedSparkContext
import org.scalatest.Suite

import org.insightedge.spark.context.InsightEdgeConfig
import org.insightedge.spark.implicits.all._

trait InsightedgeSharedContext extends SharedSparkContext {
   
  self: Suite with InsightedgeContextProvider => 
    
    override def afterAll() = {
      sc.stopInsightEdgeContext()
      super.afterAll()
    }
}