package org.insightedge.examples.financialengineering.kafka

import java.time.{ZoneId, ZonedDateTime}

import kafka.serializer.{Decoder, Encoder, StringDecoder}
import kafka.utils.VerifiableProperties
import org.insightedge.examples.financialengineering.Settings
import org.insightedge.examples.financialengineering.model.MarketTick


/**
  * User: jason
  *
  * Time: 2:27 PM
  *
  * Decodes Strings with the following format into MarketTicks:
  *
  * date | time | open | high | low | close | volume | splits | earnings | dividends
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

  def calculateTimestamp(date: String, time: String):Long = {
    val year = date.substring(0,4).toInt
    val month = date.substring(4,6).toInt
    val day = date.substring(6).toInt
    val len = time.length
    val (hour,minute) = len match {
      case 3 =>
        (("" + time.charAt(0)).toInt, time.substring(1).toInt)
      case 4 =>
        (time.substring(0,2).toInt, time.substring(2).toInt)
      case _ =>
        (0, 0) // TODO: we will fix this later
//        throw new IllegalStateException(s"time field from file has $len characters. It should be 3 or 4.")
    }
//    val tickTime = ZonedDateTime.of(year, month, day, hour, minute, 0,0, ZoneId.of(Settings.timeZone))
    val tickTime = ZonedDateTime.of(2000, 1, 1, 0, 0, 0,0, ZoneId.of(Settings.timeZone))
    tickTime.toEpochSecond
  }

  override def fromBytes(bytes: Array[Byte]): MarketTick = {
    val content = stringDecoder.fromBytes(bytes)
    println(s"content = [$content]")
    val values = content.split(",")
    val timestamp = calculateTimestamp(values(0),values(1))
    // 20160630,800,101.308,101.308,101.308,101.308,306.194,1,0,0
    MarketTick(null,
      timestamp,
      open = values(1).toDouble,
      high = values(2).toDouble,
      low = values(3).toDouble,
      close = values(4).toDouble,
      volume = values(5).toDouble,
      splits = values(6).toDouble,
      earnings = values(7).toDouble,
      dividends = if (values.length == 9) values(8).toDouble else 0
    )
  }

  override def toBytes(tick: MarketTick): Array[Byte] = {
    val timestamp = tick.timestamp
    val open = tick.open
    val high = tick.high
    val low = tick.low
    val close = tick.close
    val volume = tick.volume
    val splits = tick.splits
    val earnings = tick.earnings
    val dividends = tick.dividends
    s"$timestamp,$open,$high,$low,$close,$volume,$splits,$earnings,$dividends\n".getBytes()
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



