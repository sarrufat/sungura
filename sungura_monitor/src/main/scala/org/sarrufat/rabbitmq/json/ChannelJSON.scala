package org.sarrufat.rabbitmq.json

import spray.json.DefaultJsonProtocol
import spray.json.JsonFormat

case class Connection_details(
  name: String,
  peer_port: Int,
  peer_host: String)

case class RateDetails(
  rate: Double)
case class Message_stats(
  ack: Option[Long],
  ack_details: Option[RateDetails],
  deliver: Option[Long],
  deliver_details: Option[RateDetails],
  deliver_get: Option[Long],
  deliver_get_details: Option[RateDetails],
  publish: Option[Long],
  publish_details: Option[RateDetails])

case class ChannelJsonObject(
  connection_details: Connection_details,
  message_stats: Option[Message_stats],
  idle_since: Option[String],
  transactional: Boolean,
  confirm: Boolean,
  consumer_count: Int,
  messages_unacknowledged: Int,
  messages_unconfirmed: Int,
  messages_uncommitted: Int,
  acks_uncommitted: Int,
  prefetch_count: Int,
  global_prefetch_count: Int,
  state: String,
  node: String,
  name: String,
  number: Int,
  user: String,
  vhost: String)

case class ChannelWrapper(seq: Seq[ChannelJsonObject])
object ChannelJSONProto extends DefaultJsonProtocol {
  implicit val connection_detailsFormat = jsonFormat3(Connection_details)
  implicit val rateDetailsFormat = jsonFormat1(RateDetails)
  implicit val messageStatsFormat = jsonFormat8(Message_stats)
  implicit val channelFormat = jsonFormat18(ChannelJsonObject)
}
