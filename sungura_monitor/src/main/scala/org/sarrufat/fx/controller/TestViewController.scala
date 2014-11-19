package org.sarrufat.fx.controller

import scalafxml.core.macros.sfxml
import scalafx.event.Event
import org.sarrufat.rabbitmq.actor.MainActor
import scalafx.scene.control.ComboBox
import javafx.collections.ObservableList
import scalafx.collections.ObservableIntegerArray
import scalafx.collections.ObservableIntegerArray._
import javafx.beans.value.ObservableListValue

@sfxml
class TestViewController(val nmCombo: ComboBox[Int], val msCombo: ComboBox[Int]) {
  var numMessges: Int = 0
  var sizeMessage: Int = 0
  nmCombo.getItems().addAll(10, 50, 100, 500, 1000, 5000, 10000)
  msCombo.getItems().addAll(128, 256, 512, 1024, 2048)
  def startStopTest(ev: Event) = {
    MainActor.startTest(numMessges, sizeMessage)
  }
  def onNMSelect(ev: Event) = {
    numMessges = nmCombo.getSelectionModel.getSelectedItem
  }
  def onMSSelect(ev: Event) = {
    sizeMessage = msCombo.getSelectionModel.getSelectedItem
  }
}
