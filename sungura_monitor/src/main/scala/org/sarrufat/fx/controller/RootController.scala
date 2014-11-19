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

//class RootController(private val msgRatesChart: LineChart[String, Int]) {
@sfxml
class RootController extends Logging {
  //  println("msgRatesChart is " + msgRatesChart)
  //  val toChartData = (xy: (String, Int)) ⇒ XYChart.Data[String, Int](xy._1, xy._2)
  //  val series = new XYChart.Series[String, Int] {
  //    name = "Publish"
  //    data = Seq(("10:00:00", 0),
  //      ("10:00:10", 10),
  //      ("10:00:20", 20),
  //      ("10:00:30", 10),
  //      ("10:00:40", 4),
  //      ("10:00:50", 3),
  //      ("10:01:00", 0),
  //      ("10:00:10", 0),
  //      ("10:00:20", 0)).map(toChartData)
  //  }
  //  //  val obuf = ObservableBuffer(series)
  //  msgRatesChart.data = series
  // event handlers are simple public methods:
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
    stage.onCloseRequest = sfxei.handle {

    }
    stage.show

  }
  private def actionById(id: String) {

  }
}
