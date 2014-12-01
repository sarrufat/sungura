package org.sarrufat.fx.controller

import scalafxml.core.macros.sfxml
import scalafx.event.{ ActionEvent, EventIncludes ⇒ sfxei }
import javafx.scene.control.Button
import scalafx.scene.control.Tab
import javafx.event.Event
import javafx.{ scene ⇒ jfxs }
import scalafx.scene.image.ImageView
import scalafx.scene.chart.LineChart
import scalafx.scene.chart.XYChart
import scalafx.collections.ObservableBuffer
import scala.Int
import grizzled.slf4j.Logging
import scalafxml.core.FXMLView
import scalafxml.core.NoDependencyResolver
import scalafx.stage.Stage
import scalafx.stage.Modality
import scalafx.scene.Scene
import scalafx.scene.web.WebView
import akka.actor.Actor
import org.sarrufat.fx.model.AlarmModel
import org.sarrufat.rabbitmq.actor.MainActor
import akka.agent.Agent
import scalafx.scene.web.WebEngine
import scalafx.application.Platform
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import scala.util.Try
import scala.xml.Elem
import scalafx.scene.layout.HBox
import scalafx.geometry.Pos
import org.sarrufat.fx.controller.util.RunLaterTask

class RootControllerActor extends Actor with Logging {
  import scala.concurrent.ExecutionContext.Implicits.global
  private val modelAg = Agent(List[AlarmModel]())
  private val updatableRootAg = Agent(UpdatableRoot())
  def receive = {
    case al: AlarmModel ⇒ {
      logger.debug("receive AlarmModel")
      val fut = modelAg alter (modelAg() :+ al)
      fut.onSuccess {
        case s ⇒ Try(updatableRootAg().update(s)) match {
          case Failure(f) ⇒ logger.error(f)
          case Success(s) ⇒
        }
      }
    }
    case u: UpdatableRoot ⇒ updatableRootAg.send(u)
  }
}

object UpdatableRoot {
  def apply(): UpdatableRoot = null
}
trait UpdatableRoot extends Logging {
  def engine: WebEngine
  private val tableStyle = {
    "table.alarm {" +
      "font-family: verdana,arial,sans-serif;" +
      "font-size:11px;" +
      "color:#333333;" +
      "border-width: 1px;" +
      "border-color: #999999;" +
      "border-collapse: collapse;" +
      "}" +
      "table.alarm th {" +
      "background:#b5cfd2;" +
      "border-width: 1px;" +
      "padding: 8px;" +
      "border-style: solid;" +
      "border-color: #999999;" +
      "}" +
      "table.alarm td {" +
      "background:#dcddc0;" +
      "border-width: 1px;" +
      "padding: 8px;" +
      "border-style: solid;" +
      "border-color: #999999;" +
      "}"
  }
  def generateContent(t: Elem) = {
    <html>
      <head>
        <style>
          { tableStyle }
        </style>
      </head>
      <body>
        { t }
      </body>
    </html>
  }
  def update(mod: List[AlarmModel]) = {
    val content = generateContent(<table class="alarm">
                                    <tr>
                                      <th>Time</th><th>Message</th>
                                    </tr>
                                    { mod.map(_.xmlMessage) }
                                  </table>)

    new RunLaterTask(engine.loadContent(content.toString))
  }
}

@sfxml
class RootController(val webConsole: WebView) extends UpdatableRoot {
  val engine = webConsole.engine

  engine.loadContent(
    { <h2>without errors</h2> }.toString)
  MainActor.sender ! this
  logger.debug("this = " + this)
  def tabSelectionChanged(ev: Event) = {
    logger.debug("tabSelectionChanged : " + ev)
  }

  def openTest(ev: Event) = {
    logger.debug("openTest : " + ev)
    val testView = FXMLView(getClass.getResource("/org/sarrufat/fx/view/TestView.fxml"), NoDependencyResolver)
    val stage = new Stage
    stage.setTitle("New Test")
    stage.initModality(Modality.WINDOW_MODAL)
    val scene = new Scene(new jfxs.Scene(testView))
    stage.setScene(scene)

    stage.show

  }
  def openSetup(ev: Event) = {
    val testView = FXMLView(getClass.getResource("/org/sarrufat/fx/view/SetupView.fxml"), NoDependencyResolver)
    val stage = new Stage
    stage.setTitle("Setup")
    stage.initModality(Modality.APPLICATION_MODAL)
    val scene = new Scene(new jfxs.Scene(testView))
    stage.setScene(scene)

    stage.show
  }
}
