package org.sarrufat.rabbitmq.json

import spray.json.DefaultJsonProtocol
import spray.json.JsonFormat

case class Recv_oct_details(rate: Double)
case class Capabilities(
  exchange_exchange_bindings: Boolean,
  consumer_cancel_notify: Boolean,
  //  basic_nack: Boolean,
  publisher_confirms: Boolean)
case class Client_properties(
  product: String,
  information: String,
  platform: String,
  capabilities: Capabilities,
  copyright: String,
  version: String)
case class ConnectionsJSON(
  recv_oct: Long,
  recv_oct_details: Recv_oct_details,
  send_oct: Long,
  send_oct_details: Recv_oct_details,
  recv_cnt: Int,
  send_cnt: Int,
  send_pend: Int,
  state: String,
  channels: Int,
  // _type: String,
  //  node: String,
  name: String,
  //  port: Int,
  //  peer_port: Int,
  //  host: String,
  //  peer_host: String,
  //  ssl: Boolean,
  //  peer_cert_subject: String,
  //  peer_cert_issuer: String,
  //  peer_cert_validity: String,
  //  auth_mechanism: String,
  //  ssl_protocol: String,
  //  ssl_key_exchange: String,
  //  ssl_cipher: String,
  //  ssl_hash: String,
  protocol: String,
  user: String,
  vhost: String,
  timeout: Int,
  frame_max: Int,
  channel_max: Int,
  client_properties: Client_properties)

object ConnectionsJSONProto extends DefaultJsonProtocol {
  implicit val recv_oct_detailsFormat = jsonFormat1(Recv_oct_details)
  implicit val capabilitiesFormat = jsonFormat3(Capabilities)
  implicit val clientPropertiesFormat = jsonFormat6(Client_properties)
  implicit val connectionsJSONFormat = jsonFormat17(ConnectionsJSON)
}
