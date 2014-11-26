package org.sarrufat.rabbitmq.json

import spray.json.DefaultJsonProtocol

case class OwnerPidDetails(
  name: String,
  peer_port: Int,
  peer_host: String)
case class MessagesDetails(
  rate: Double)
//case class BackingQueueStatus(
//  q1: Double,
//  q2: Double,
//  delta: String,
//  q3: Double,
//  q4: Double,
//  len: Double,
//  pending_acks: Double,
//  target_ram_count: String,
//  ram_msg_count: Double,
//  ram_ack_count: Double,
//  next_seq_id: Double,
//  persistent_count: Double,
//  avg_ingress_rate: Double,
//  avg_egress_rate: Double,
//  avg_ack_ingress_rate: Double,
//  avg_ack_egress_rate: Double)
//case class Arguments()
case class QueueJsonObject(
  message_stats: Option[MessagesStats],
  memory: Long,
  owner_pid_details: Option[OwnerPidDetails],
  messages: Long,
  messages_details: MessagesDetails,
  messages_ready: Long,
  messages_ready_details: MessagesDetails,
  messages_unacknowledged: Long,
  messages_unacknowledged_details: MessagesDetails,
  idle_since: Option[String],
  //  consumer_utilisation: Double,
  policy: String,
  exclusive_consumer_tag: String,
  consumers: Int,
  //  backing_queue_status: BackingQueueStatus,
  //  state: String,
  name: String,
  vhost: String,
  durable: Boolean,
  auto_delete: Boolean,
  //  arguments: Arguments,
  node: String)

object QueueJsonProto extends DefaultJsonProtocol {
  implicit val detailsFormat = jsonFormat1(Details)
  implicit val messagesStatFormat = jsonFormat14(MessagesStats)
  implicit val ownerPidDetailsFormat = jsonFormat3(OwnerPidDetails)
  implicit val messagesDetailsFormat = jsonFormat1(MessagesDetails)
  implicit val queueJsonFormat = jsonFormat18(QueueJsonObject)
}

case class QueueJsonWrapper(seq: Seq[QueueJsonObject])
