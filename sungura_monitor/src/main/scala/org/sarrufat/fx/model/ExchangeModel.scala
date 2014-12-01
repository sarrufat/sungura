package org.sarrufat.fx.model

import org.sarrufat.rabbitmq.json.ExchangeJsonObject
import scalafx.beans.property.StringProperty
import scalafx.scene.control.TableColumn
import org.sarrufat.rabbitmq.json.ExRateDetails

object ExchangeModel {
  private val colnames = List("Name", "Type", "Prameters", "Messages rate in", "Messages rate out")
  private val _tableColumns = List(
    new TableColumn[ExchangeModel, String] { cellValueFactory = { _.value.name } },
    new TableColumn[ExchangeModel, String] { cellValueFactory = { _.value.etype } },
    new TableColumn[ExchangeModel, String] { cellValueFactory = { _.value.parameters } },
    new TableColumn[ExchangeModel, String] { cellValueFactory = { _.value.rateIn } },
    new TableColumn[ExchangeModel, String] { cellValueFactory = { _.value.rateOut } })
  def tableColumns = {
    colnames zip _tableColumns foreach (tup ⇒ tup._2.text = tup._1)
    _tableColumns foreach (_.sortable = false)
    _tableColumns.map(tc ⇒ tc.delegate)
  }
}
class ExchangeModel(info: ExchangeJsonObject) {
  val name = new StringProperty(this, "name", getName)
  val etype = new StringProperty(this, "type", info.typeE)
  val parameters = new StringProperty(this, "param", getParameters)
  val rateIn = new StringProperty(this, "param", getRateIn)
  val rateOut = new StringProperty(this, "param", getRateOut)

  private def getDet(rate: Option[ExRateDetails]) = {
    rate match {
      case Some(r) ⇒ r.rate
      case None    ⇒ 0.0
    }
  }
  private def getRateIn = {
    info.message_stats match {
      case Some(mst) ⇒ getDet(mst.publish_in_details) + "/s"
      case None      ⇒ ""
    }
  }
  private def getRateOut = {
    info.message_stats match {
      case Some(mst) ⇒ getDet(mst.publish_out_details) + "/s"
      case None      ⇒ ""
    }
  }
  private def getName = {
    info.name match {
      case "" ⇒ "(AMQP default)"
      case _  ⇒ info.name
    }
  }
  private def getParameters = {
    def getDurable = {
      if (info.durable)
        " Durable "
      else
        ""
    }
    def getAutoDel = {
      if (info.auto_delete)
        " Auto.Del. "
      else
        ""
    }
    def getAutoInter = {
      if (info.internal)
        " Internal "
      else
        ""
    }
    getDurable + getAutoDel + getAutoInter
  }
}
