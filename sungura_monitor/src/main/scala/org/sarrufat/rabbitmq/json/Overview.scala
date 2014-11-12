package org.sarrufat.rabbitmq.json

import spray.json.DefaultJsonProtocol
import spray.json.JsonFormat
import scala.Double
import java.time.LocalTime
import java.time.format.DateTimeFormatter

case class Overview(val management_version: String, val statistics_level: String, val rabbitmq_version: String, val erlang_version: String, val node: String, val erlang_full_version: String, val statistics_db_node: String, val exchange_types: List[ExchangeType], val message_stats: MessagesStats) {
  override def toString = "message_stats{ " + message_stats + "}"
}
case class ExchangeType(val name: String, val description: String, val enabled: Boolean)
case class Details(val rate: Double)
case class MessagesStats(val publish: Option[Long], val ack: Option[Long], val deliver_get: Option[Long], val redeliver: Option[Long], val deliver: Option[Long], val deliver_no_ack: Option[Long], val get: Option[Long], val publish_details: Option[Details], val ack_details: Option[Details], val deliver_get_details: Option[Details], val redeliver_details: Option[Details], val deliver_details: Option[Details], val deliver_no_ack_details: Option[Details], val get_details: Option[Details]) {
  override def toString = {
    "publish_details: " + publish_details + " ack_details: " + " deliver_get_details: " + deliver_get_details + " redeliver_details: " + redeliver_details + " deliver_details: " + deliver_details + " deliver_no_ack_details: " + deliver_no_ack_details + " get_details:" + get_details
  }
}

case class RabbitMQResult[T](result: T)

object OverviewProtocol extends DefaultJsonProtocol {
  implicit val detailsFormat = jsonFormat1(Details)
  implicit val messagesStatFormat = jsonFormat14(MessagesStats)
  implicit val extypeFormat = jsonFormat3(ExchangeType)
  implicit val overviewFormat = jsonFormat9(Overview)
  implicit def overviewResultFormat[T: JsonFormat] = jsonFormat1(RabbitMQResult.apply[T])
}

class OverviewWTS(val stime: String, val ov: Overview) {
  private def getOptionInt(det: Option[Details]) = {
    det match {
      case Some(x) ⇒ x.rate.toInt
      case None    ⇒ 0
    }
  }
  def pubDet = getOptionInt(ov.message_stats.publish_details)
  def ackDet = getOptionInt(ov.message_stats.ack_details)
  def delvDet = getOptionInt(ov.message_stats.deliver_details)
  def delvNoCackDet = getOptionInt(ov.message_stats.deliver_no_ack_details)
  def delGetDet = getOptionInt(ov.message_stats.deliver_get_details)
  def reDelDet = getOptionInt(ov.message_stats.redeliver_details)
  def getDet = getOptionInt(ov.message_stats.get_details)
}

object OverviewWTS {
  private val dtf = DateTimeFormatter.ofPattern("mm:ss")
  def apply(ov: Overview) = {
    new OverviewWTS(dtf.format(LocalTime.now), ov)
  }
}
