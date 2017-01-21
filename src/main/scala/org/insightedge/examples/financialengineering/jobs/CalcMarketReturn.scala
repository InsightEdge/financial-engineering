package org.insightedge.examples.financialengineering.jobs

import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.insightedge.examples.financialengineering
import org.insightedge.examples.financialengineering.model.{MarketTick, TickData, TickerSymbols}
import org.insightedge.examples.financialengineering.{Settings, makeStreamingContext}
import org.insightedge.spark.implicits.all._

/**
  * Created by IntelliJ IDEA.
  * User: jason
  * Date: 1/20/17
  * Time: 12:21 PM
  */
class CalcMarketReturn {


}
