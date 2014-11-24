package org.sarrufat.fx.model

import org.sarrufat.rabbitmq.json.ChannelJsonObject
import ChannelModel._
import scalafx.beans.property.StringProperty
import scalafx.scene.control.TableColumn
import org.sarrufat.rabbitmq.json.RateDetails

object ChannelModel {
  private val colnames = List("Name", "User", "Mode", "Prefetch", "State", "Idle Since", "Consumers")
  private val _tableColumns = List(
    new TableColumn[ChannelModel, String] { cellValueFactory = { _.value.pname } },
    new TableColumn[ChannelModel, String] { cellValueFactory = { _.value.user } },
    new TableColumn[ChannelModel, String] { cellValueFactory = { _.value.mode } },
    new TableColumn[ChannelModel, String] { cellValueFactory = { _.value.prefetch } },
    new TableColumn[ChannelModel, String] {
      cellValueFactory = { _.value.state }
      cellFactory = { c ⇒
        new StateTableCell[ChannelModel]
      }
    },
    new TableColumn[ChannelModel, String] { cellValueFactory = { _.value.idleSince } },
    new TableColumn[ChannelModel, String] { cellValueFactory = { _.value.consumer_count } })
  def tableColumns = {
    colnames zip _tableColumns foreach (tup ⇒ tup._2.text = tup._1)
    _tableColumns.map(tc ⇒ tc.delegate)
  }
  private def bool2String(v: Boolean, sim: String) = {
    v match {
      case true  ⇒ sim
      case false ⇒ " "
    }
  }
}
class ChannelModel(info: ChannelJsonObject) {
  val pname = new StringProperty(this, "name", info.name)
  val user = new StringProperty(this, "user", info.user)
  val mode = new StringProperty(this, "mode", bool2String(info.transactional, "T") + " " + bool2String(info.confirm, "C"))
  val prefetch = new StringProperty(this, "prefetch", info.prefetch_count.toString)
  val state = new StringProperty(this, "state", stateFromMStat)
  val idleSince = new StringProperty(this, "idleSince", info.idle_since.getOrElse(""))
  val consumer_count = new StringProperty(this, "consumer_count", info.consumer_count.toString)

  private def stateFromMStat = {
    def stateFromRates = {
      def checkIdle(rate: Option[RateDetails]): Boolean = {
        rate match {
          case Some(r) ⇒ r.rate > 0.0
          case None    ⇒ false
        }
      }
      val mgsStat = info.message_stats.get
      if (checkIdle(mgsStat.ack_details) || checkIdle(mgsStat.deliver_details) || checkIdle(mgsStat.deliver_get_details) || checkIdle(mgsStat.publish_details))
        "running"
      else
        "idle"
    }
    info.message_stats match {
      case Some(ms) ⇒ stateFromRates
      case None     ⇒ "idle"
    }
  }
}
