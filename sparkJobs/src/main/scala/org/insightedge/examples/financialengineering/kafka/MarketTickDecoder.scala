package org.insightedge.examples.financialengineering.kafka

import java.time.{ZoneId, ZonedDateTime}

import _root_.kafka.serializer.{Decoder, Encoder, StringDecoder}
import kafka.utils.VerifiableProperties
import org.insightedge.examples.financialengineering.CoreSettings
import org.insightedge.examples.financialengineering.model.MarketTick


/**
  * User: jason
  *
  * Time: 2:27 PM
  *
  * Decodes Strings with the following format into MarketTicks:
  *
  * timestamp | open | high | low | close | volume | splits | earnings | dividends
  *
  * 20160630,800,101.308,101.308,101.308,101.308,306.194,1,0,0
  *
  * Encodes strings with that format
  * org.apache.kafka.common.serialization.Serializer
  */
class MarketTickDecoder extends Decoder[MarketTick] with Encoder[MarketTick] {

  val stringDecoder = new StringDecoder()

  def this(verifiableProperties: VerifiableProperties) {
    this
  }

  override def fromBytes(bytes: Array[Byte]): MarketTick = {
    val values = stringDecoder.fromBytes(bytes).split(",")
    new MarketTick(
      timestamp = values(0).toLong,
      dateAsStr = values(1),
      open = values(2).toDouble,
      high = values(3).toDouble,
      low = values(4).toDouble,
      close = values(5).toDouble,
      volume = values(6).toDouble,
      splits = values(7).toDouble,
      earnings = values(8).toDouble,
      dividends = if (values.length == 10) values(9).toDouble else 0
    )
  }

  override def toBytes(tick: MarketTick): Array[Byte] = {
    (tick + "\n").getBytes()
  }
}

import org.apache.kafka.common.serialization.Serializer

class MarketTickSerializer extends Serializer[MarketTick] {

  val decoder = new MarketTickDecoder

  override def serialize(topic: java.lang.String, data: MarketTick): Array[Byte] = {
    decoder.toBytes(data)
  }

  override def configure(configs: _root_.java.util.Map[_root_.java.lang.String, _], isKey: Boolean): Unit = {}

  override def close(): Unit = {}

}



