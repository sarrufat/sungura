package org.sarrufat.rabbitmq.json

import spray.json.DefaultJsonProtocol
import spray.json.JsonFormat

case class Connection_details(
  name: String,
  peer_port: Int,
  peer_host: String)

case class ChannelJsonObject(
  connection_details: Connection_details,
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
  implicit val channelFormat = jsonFormat17(ChannelJsonObject)
}
