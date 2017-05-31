package org.insightedge.examples.financialengineering.applications

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import org.apache.kafka.clients.producer.KafkaProducer
import org.insightedge.examples.financialengineering.KafkaSettings
import org.insightedge.examples.financialengineering.kafka.MarketTickDecoder
import org.insightedge.examples.financialengineering.model.MarketTick
import org.insightedge.examples.financialengineering.repos.TickerSymbols

/**
  * User: jason nerothin
  *
  * Time: 5:39 AM
  * Pulls data from a csv file and writes it to Kafka.
  *
  * The file has the ticker symbol in its name. It has a header line
  * and this format:
  *
  * date | time | open | high | low | close | volume | splits | earnings | dividends
  *
  */
object Feed {

  /**
    * @param args. A thread count can be passed
    */
  def main(args: Array[String]) = {
    if (args.length < 1) {
      System.err.println("Usage: Feed <feed data directory> [thread count]")
      System.exit(1)
    }
    val baseDir = args(0)
    val producer = new KafkaProducer[String, MarketTick](KafkaSettings.kafkaProducerProperties())
     
    val pool: ExecutorService = Executors.newFixedThreadPool(if (args.length > 1) args(1).toInt else Runtime.getRuntime.availableProcessors())
    try {
      TickerSymbols.all().map(t => {
        pool.submit(new MarketTickProducer(baseDir, t, producer))
      })
    } finally {
      pool.shutdown()
      if (!pool.awaitTermination(10, TimeUnit.MINUTES)) {
        pool.shutdownNow()
      }
      producer.close()
    }
  }
}