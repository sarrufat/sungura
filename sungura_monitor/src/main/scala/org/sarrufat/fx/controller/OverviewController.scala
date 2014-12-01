package org.sarrufat.fx.controller

import org.sarrufat.rabbitmq.actor.MainActor
import org.sarrufat.rabbitmq.json.{ OverviewWTS, Overview, _ }
import akka.actor.Actor
import akka.agent.Agent
import grizzled.slf4j.{ Logger, Logging }
import javafx.{ event ⇒ jfxe, scene ⇒ jfxs, stage ⇒ jfxst }
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.event.{ ActionEvent, EventIncludes ⇒ sfxei }
import scalafx.scene.Scene
import scalafx.scene.chart.{ LineChart, XYChart }
import scalafx.stage.{ Modality, Stage }
import scalafxml.core.{ FXMLView, NoDependencyResolver }
import scalafxml.core.macros.sfxml
import org.sarrufat.rabbitmq.actor.ResetModel
import org.sarrufat.fx.controller.util.RunLaterTask

class OverviewTask extends Logging {
  new RunLaterTask(OverviewControllerActor.controller.setDatas(OverviewControllerActor.getAgent))
}

trait OVController {
  def setDatas(values: OverviewControllerActor.OVValue): Unit
}

object OverviewControllerActor {
  type OVValue = List[OverviewWTS]
  //  lazy val sender = ActorSystem().actorOf(Props[OverviewControllerActor])
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
  private def resetModel = ovAgent send List[OverviewWTS]()
}

class OverviewControllerActor extends Actor {
  def receive = {
    case ov: OverviewWTS ⇒ {
      OverviewControllerActor.updateAgent(ov)
      new OverviewTask
    }
    case ResetModel ⇒ {
      OverviewControllerActor.resetModel
      new OverviewTask
    }
  }
}

@sfxml
class OverviewController(private val msgRatesChart: LineChart[String, Int], val queueTotalsChart: LineChart[String, Long]) extends OVController {
  //  val logger = Logger[this.type]

  val toChartData = (xy: (String, Int)) ⇒ XYChart.Data[String, Int](xy._1, xy._2)
  val toChartLData = (xy: (String, Long)) ⇒ XYChart.Data[String, Long](xy._1, xy._2)
  private var connectionsOpen = false
  MainActor.startOverview
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

    setMessagesStats
    setQueueTotlas

  }

  def onConnections(event: ActionEvent) {
    if (!connectionsOpen) {
      connectionsOpen = true
      val connLayout = FXMLView(getClass.getResource("/org/sarrufat/fx/view/ConnectionsView.fxml"), NoDependencyResolver)
      val stage = new Stage
      stage.setTitle("Connections")
      stage.initModality(Modality.NONE)
      val scene = new Scene(new jfxs.Scene(connLayout))
      stage.setScene(scene)
      stage.onCloseRequest = sfxei.handle {
        MainActor.stopConnections
        connectionsOpen = false
      }
      stage.show
    }

  }
  OverviewControllerActor.controller = this

}
