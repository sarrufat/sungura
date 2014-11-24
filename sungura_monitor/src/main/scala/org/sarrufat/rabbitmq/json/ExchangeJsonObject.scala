package org.sarrufat.rabbitmq.json

import spray.json.DefaultJsonProtocol
import spray.json.JsonFormat
import scala.Option

case class ExRateDetails(rate: Double)

case class ExMessageStats(
  publish_in: Option[Long],
  publish_in_details: Option[ExRateDetails],
  publish_out: Option[Long],
  publish_out_details: Option[ExRateDetails])

case class ExchangeJsonObject(
  message_stats: Option[ExMessageStats],
  name: String,
  vhost: String,
  typeE: String,
  durable: Boolean,
  auto_delete: Boolean,
  internal: Boolean)

case class ExchangeWrapper(seq: Seq[ExchangeJsonObject])

object ExchangeJsonProto extends DefaultJsonProtocol {
  implicit val exRateDetailsFormat = jsonFormat1(ExRateDetails)
  implicit val exMessageStatsFormat = jsonFormat4(ExMessageStats)
  implicit val exchangeFormat = jsonFormat(ExchangeJsonObject, "message_stats", "name", "vhost", "type", "durable", "auto_delete", "internal")
}
