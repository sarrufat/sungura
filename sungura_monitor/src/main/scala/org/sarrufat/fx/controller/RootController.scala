package org.sarrufat.fx.controller

import scalafxml.core.macros.sfxml
import scalafx.event.ActionEvent
import javafx.scene.control.Button
import scalafx.scene.control.Tab
import javafx.event.Event
import scalafx.scene.image.ImageView
import scalafx.scene.chart.LineChart
import scalafx.scene.chart.XYChart
import scalafx.collections.ObservableBuffer
import scala.Int

//class RootController(private val msgRatesChart: LineChart[String, Int]) {
@sfxml
class RootController {
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
    println("tabSelectionChanged : " + ev)
  }

  private def actionById(id: String) {

  }
}
