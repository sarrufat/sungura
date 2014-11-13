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
import scalafx.scene.layout.AnchorPane
import scalafx.scene.control.Button
import scalafx.event.ActionEvent
import scalafxml.core.FXMLView
import scalafxml.core.NoDependencyResolver
import scalafx.stage.Stage
import scalafx.stage.Modality
import scalafx.scene.Scene
import javafx.{ scene ⇒ jfxs }

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
class OverviewController(private val msgRatesChart: LineChart[String, Int], val queueTotalsChart: LineChart[String, Long], val connectionsButton: Button, val channelsButton: Button, val exchangesButton: Button, val queuesButton: Button, val consumersButton: Button) extends OVController {
  val logger = Logger[this.type]

  val toChartData = (xy: (String, Int)) ⇒ XYChart.Data[String, Int](xy._1, xy._2)
  val toChartLData = (xy: (String, Long)) ⇒ XYChart.Data[String, Long](xy._1, xy._2)

  //  val obuf = ObservableBuffer(series)
  override def setDatas(values: OverviewControllerActor.OVValue) = {
    def setMessagesStats = {
      def createSerie(nam: String)(fv: OverviewWTS ⇒ Int) = {
        new XYChart.Series[String, Int] {
          name = nam
          data = values.map(v ⇒ (v.stime, fv(v))).map(toChartData)
        }
      }
      val seriesp = createSerie("Publish") { v ⇒ v.pubDet }
      val seriesa = createSerie("Ack") { v ⇒ v.ackDet }
      val seriesdel = createSerie("Deliver") { v ⇒ v.delGetDet }
      val seriesdelNA = createSerie("Deliver (noack)") { v ⇒ v.delvNoCackDet }
      val seriesReDel = createSerie("Redelivered") { v ⇒ v.reDelDet }
      val seriesGet = createSerie("Get") { v ⇒ v.getDet }
      msgRatesChart.data = ObservableBuffer(seriesp.delegate, seriesdel.delegate, seriesReDel.delegate, seriesa.delegate, seriesGet.delegate, seriesdelNA.delegate)
    }
    def setQueueTotlas = {
      def createSerie(nam: String)(fv: OverviewWTS ⇒ Long) = {
        new XYChart.Series[String, Long] {
          name = nam
          data = values.map(v ⇒ (v.stime, fv(v))).map(toChartLData)
        }
      }
      val qReadySerie = createSerie("Ready") { v ⇒ v.ov.queue_totals.messages_ready }
      val qTotalSerie = createSerie("Total") { v ⇒ v.ov.queue_totals.messages }
      val unackSerie = createSerie("Unack.") { v ⇒ v.ov.queue_totals.messages_unacknowledged }
      queueTotalsChart.data = ObservableBuffer(qReadySerie.delegate, unackSerie.delegate, qTotalSerie.delegate)
    }
    def setButtonsLables = {
      val lastOv = values.last
      connectionsButton.setText("Connections " + lastOv.ov.object_totals.connections)
      channelsButton.setText("Channels " + lastOv.ov.object_totals.channels)
      exchangesButton.setText("Exchanges " + lastOv.ov.object_totals.exchanges)
      queuesButton.setText("Queues " + lastOv.ov.object_totals.queues)
      consumersButton.setText("Consumers " + lastOv.ov.object_totals.consumers)
    }
    setMessagesStats
    setQueueTotlas
    setButtonsLables
  }

  def onConnections(event: ActionEvent) {
    val connLayout = FXMLView(getClass.getResource("/org/sarrufat/fx/view/ConnectionsView.fxml"), NoDependencyResolver)
    val stage = new Stage
    stage.setTitle("Connections")
    stage.initModality(Modality.APPLICATION_MODAL)
    val scene = new Scene(new jfxs.Scene(connLayout))
    stage.setScene(scene)
    stage.show
  }

  OverviewControllerActor.controller = this

}
