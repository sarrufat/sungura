package org.sarrufat.fx.controller

import scalafxml.core.macros.sfxml
import scalafx.scene.control.TextField
import org.sarrufat.rabbitmq.actor.MainActor
import org.sarrufat.rabbitmq.actor.MainActor
import javafx.event.Event
import javafx.scene.Node
import javafx.stage.Stage

@sfxml
class SetupController(val hostField: TextField) {
  hostField.text = MainActor.hostName
  MainActor.stopAll
  def onSetup(ev: Event) = {
    MainActor.hostName = hostField.text.value
    MainActor.startAll
    val dev = ev
    dev.getTarget.asInstanceOf[Node].getScene.getWindow.asInstanceOf[Stage].close()
  }
  def onClose(ev: Event) = {
    MainActor.startAll
  }
}
