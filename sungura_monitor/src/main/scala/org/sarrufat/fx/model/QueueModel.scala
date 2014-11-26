package org.sarrufat.fx.model

import org.sarrufat.rabbitmq.json.QueueJsonObject
import scalafx.beans.property.StringProperty
import scalafx.scene.control.TableColumn
import scalafx.beans.property.LongProperty
import org.sarrufat.rabbitmq.json.Details
object QueueModel {
  private val colnames = List("Name", "Exclusive", "Parameters", "State", "Ready", "Unacked", "Total", "Incoming", "Deliver/Get", "Ack")
  private val _tableColumns = List(
    new TableColumn[QueueModel, String] { cellValueFactory = { _.value.name } },
    new TableColumn[QueueModel, String] { cellValueFactory = { _.value.exclusive } },
    new TableColumn[QueueModel, String] { cellValueFactory = { _.value.parameters } },
    new TableColumn[QueueModel, String] {
      cellValueFactory = { _.value.state }
      cellFactory = { c ⇒
        new StateTableCell[QueueModel]
      }
    },
    new TableColumn[QueueModel, String] { cellValueFactory = { _.value.ready } },
    new TableColumn[QueueModel, String] { cellValueFactory = { _.value.unacked } },
    new TableColumn[QueueModel, String] { cellValueFactory = { _.value.total } },
    new TableColumn[QueueModel, String] { cellValueFactory = { _.value.incoming } },
    new TableColumn[QueueModel, String] { cellValueFactory = { _.value.delGet } },
    new TableColumn[QueueModel, String] { cellValueFactory = { _.value.ack } })

  def tableColumns = {
    colnames zip _tableColumns foreach (tup ⇒ tup._2.text = tup._1)
    _tableColumns foreach (_.sortable = false)
    _tableColumns.map(tc ⇒ tc.delegate)
  }
}
class QueueModel(info: QueueJsonObject) {
  val name = new StringProperty(this, "name", info.name)
  val exclusive = new StringProperty(this, "exclusive", getExclusive)
  val parameters = new StringProperty(this, "parameters", getParameters)
  val state = new StringProperty(this, "state", getState)
  val ready = new StringProperty(this, "ready", info.messages_ready.toString)
  val unacked = new StringProperty(this, "unacked", info.messages_unacknowledged.toString)
  val total = new StringProperty(this, "total", info.messages.toString)
  val incoming = new StringProperty(this, "incoming", getIncoming)
  val delGet = new StringProperty(this, "delGet", getDelGet)
  val ack = new StringProperty(this, "ack", getAck)

  private def getExclusive = {
    info.owner_pid_details match {
      case Some(x) ⇒ "Owner"
      case None    ⇒ ""
    }
  }
  private def getParameters = {
    def getDurable = {
      info.durable match {
        case true  ⇒ "Dur."
        case false ⇒ ""
      }
    }
    def getAutoDel = {
      info.auto_delete match {
        case true  ⇒ "AutoDel."
        case false ⇒ ""
      }
    }
    getAutoDel + getDurable
  }
  private def getRate(rate: Option[Details]) = {
    rate match {
      case Some(r) ⇒ r.rate.toString + "/s"
      case None    ⇒ ""
    }
  }
  private def getRateD(rate: Option[Details]) = {
    rate match {
      case Some(r) ⇒ r.rate
      case None    ⇒ 0.0
    }
  }
  private def getIncoming = {
    info.message_stats match {
      case Some(st) ⇒ getRate(st.publish_details)
      case None     ⇒ ""
    }
  }
  private def getDelGet = {
    info.message_stats match {
      case Some(st) ⇒ getRate(st.deliver_get_details)
      case None     ⇒ ""
    }
  }
  private def getAck = {
    info.message_stats match {
      case Some(st) ⇒ getRate(st.ack_details)
      case None     ⇒ ""
    }
  }
  private def getState = {
    info.message_stats match {
      case Some(st) ⇒ {
        if (getRateD(st.publish_details) > 0.0 || getRateD(st.deliver_get_details) > 0.0 || getRateD(st.ack_details) > 0.0)
          "running"
        else
          "idle"
      }
      case None ⇒ "idle"
    }
  }
}
