package org.sarrufat.rabbitmq.json

import spray.json.DefaultJsonProtocol
import spray.json.JsonFormat

case class Overview(val management_version: String, val statistics_level: String, val rabbitmq_version: String, val erlang_version: String, val node: String, val erlang_full_version: String, val statistics_db_node: String, val exchange_types: List[ExchangeType], val message_stats: MessagesStats)
case class ExchangeType(val name: String, val description: String, val enabled: Boolean)
case class MessagesStats(val publish: Option[Long], val ack: Option[Long], val deliver_get: Option[Long], val redeliver: Option[Long], val deliver: Option[Long], val deliver_no_ack: Option[Long], val get: Option[Long])
case class RabbitMQResult[T](result: T)

object OverviewProtocol extends DefaultJsonProtocol {
  implicit val messagesStatFormat = jsonFormat7(MessagesStats)
  implicit val extypeFormat = jsonFormat3(ExchangeType)
  implicit val overviewFormat = jsonFormat9(Overview)
  implicit def overviewResultFormat[T: JsonFormat] = jsonFormat1(RabbitMQResult.apply[T])
}
