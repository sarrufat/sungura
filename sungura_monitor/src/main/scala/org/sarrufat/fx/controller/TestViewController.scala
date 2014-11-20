package org.sarrufat.fx.controller

import scalafxml.core.macros.sfxml
import scalafx.event.Event
import org.sarrufat.rabbitmq.actor.MainActor
import scalafx.scene.control.ComboBox
import javafx.collections.ObservableList
import scalafx.collections.ObservableIntegerArray
import scalafx.collections.ObservableIntegerArray._
import javafx.beans.value.ObservableListValue
import grizzled.slf4j.Logging
import javafx.scene.Node
import javafx.stage.Stage

@sfxml
class TestViewController(val nmCombo: ComboBox[Int], val msCombo: ComboBox[Int]) extends Logging {
  var numMessges: Int = 10
  var sizeMessage: Int = 128

  nmCombo.getItems().addAll(10, 50, 100, 500, 1000, 5000, 10000, 100000)
  msCombo.getItems().addAll(128, 256, 512, 1024, 2048, 4096, 8192)
  def startStopTest(ev: Event) = {
    MainActor.startTest(nmCombo.value.value, msCombo.value.value)
    val dev = ev.delegate
    dev.getTarget.asInstanceOf[Node].getScene.getWindow.asInstanceOf[Stage].close()
  }
  //  def onNMSelect(ev: Event) = {
  //    logger.debug(ev)
  //    numMessges = nmCombo.getSelectionModel.getSelectedItem
  //  }
  //  def onMSSelect(ev: Event) = {
  //    logger.debug(ev)
  //    sizeMessage = msCombo.getSelectionModel.getSelectedItem
  //  }

  nmCombo.value = 10
  msCombo.value = 128

}
