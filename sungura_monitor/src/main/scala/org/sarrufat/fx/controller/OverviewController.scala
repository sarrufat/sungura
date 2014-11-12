package org.sarrufat.fx.controller

import org.sarrufat.rabbitmq.json._
import scalafxml.core.macros.sfxml
import scalafx.scene.chart.LineChart
import scalafx.scene.chart.XYChart
import scalafx.concurrent.Task
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorSystem
import grizzled.slf4j.Logger
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import scalafx.application.Platform
import org.sarrufat.rabbitmq.json.Overview
import akka.agent.Agent
import org.sarrufat.rabbitmq.json.OverviewWTS
import scalafx.collections.ObservableBuffer
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import javafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer

class OverviewTask {
  val logger = Logger[this.type]
  private val sjxTask = Task[String] {

    Platform.runLater(
      Try(OverviewControllerActor.controller.setDatas(OverviewControllerActor.getAgent)) match {
        case Success(s) ⇒ logger.debug("running  OverviewTask OK")
        case Failure(e) ⇒ logger.error("Error: " + e)
      })
    "Resultado"
  }
  def run = sjxTask.run
}

trait OVController {
  def setDatas(values: OverviewControllerActor.OVValue): Unit
}

object OverviewControllerActor {
  type OVValue = List[OverviewWTS]
  lazy val sender = ActorSystem().actorOf(Props[OverviewControllerActor])
  private var vcont: OVController = null
  def controller = vcont
  def controller_=(c: OVController) = { vcont = c }
  import scala.concurrent.ExecutionContext.Implicits.global
  private val ovAgent = Agent(List[OverviewWTS]())
  def updateAgent(ov: OverviewWTS) = {
    val actual = ovAgent.get
    val newl = actual.length match {
      case x if x >= 24 ⇒ (actual :+ ov) drop ((x + 1) - 24)
      case _            ⇒ (actual :+ ov)
    }
    ovAgent send (newl)
  }
  def getAgent = ovAgent.get
}

class OverviewControllerActor extends Actor {
  def receive = {
    case ov: OverviewWTS ⇒ {
      OverviewControllerActor.updateAgent(ov)
      val task = new OverviewTask
      task.run
    }
  }
}

@sfxml
class OverviewController(private val msgRatesChart: LineChart[String, Int]) extends OVController {
  val toChartData = (xy: (String, Int)) ⇒ XYChart.Data[String, Int](xy._1, xy._2)

  //  val obuf = ObservableBuffer(series)
  override def setDatas(values: OverviewControllerActor.OVValue) = {

    val seriesp = new XYChart.Series[String, Int] {
      name = "Publish"
      data = values.map(v ⇒ (v.stime, v.pubDet)).map(toChartData)
    }
    val seriesa = new XYChart.Series[String, Int] {
      name = "Ack"
      data = values.map(v ⇒ (v.stime, v.ackDet)).map(toChartData)
    }

    val seriesdel = new XYChart.Series[String, Int] {
      name = "Deliver"
      data = values.map(v ⇒ (v.stime, v.delvDet + v.delGetDet)).map(toChartData)
    }
    val seriesdelNA = new XYChart.Series[String, Int] {
      name = "Deliver (noack)"
      data = values.map(v ⇒ (v.stime, v.delvNoCackDet)).map(toChartData)
    }
    val seriesReDel = new XYChart.Series[String, Int] {
      name = "Redelivered"
      data = values.map(v ⇒ (v.stime, v.reDelDet)).map(toChartData)
    }

    val seriesGet = new XYChart.Series[String, Int] {
      name = "Get"
      data = values.map(v ⇒ (v.stime, v.getDet)).map(toChartData)
    }

    msgRatesChart.data = ObservableBuffer(seriesp.delegate, seriesdel.delegate, seriesReDel.delegate, seriesa.delegate, seriesGet.delegate, seriesdelNA.delegate)
  }
  OverviewControllerActor.controller = this

}
