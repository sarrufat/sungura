package org.sarrufat.fx.controller

import scala.util._
import org.sarrufat.rabbitmq.actor.MainActor
import scalafxml.core.macros.sfxml
import scalafx.scene.control.TableView
import org.sarrufat.fx.model.{ ModelConnection, ChannelModel }
import scalafx.collections.ObservableBuffer
import grizzled.slf4j.Logging
import scalafx.concurrent.Task
import scalafx.application.Platform
import javafx.scene.control.TreeTableView
import scalafx.scene.layout.AnchorPane
import javafx.scene.control.TreeTableView
import org.sarrufat.fx.model.ChannelModel
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue

class ConnectionsUpdaterTask extends Logging {
  private val sjxTask = Task[Unit] {
    Platform.runLater(
      Try(ConnectionsControllerStore.controller.updateModel(ConnectionsControllerActor.getModel)) match {
        case Success(s) ⇒ logger.debug("running  ConnectionsUpdaterTask OK")
        case Failure(e) ⇒ logger.error("Error: " + e)
      })
  }
  def run = sjxTask.run
}
class ChannelUpdaterTask extends Logging {
  private val sjxTask = Task[Unit] {
    Platform.runLater(
      Try(ConnectionsControllerStore.controller.updateChannModel(ConnectionsControllerActor.getChannModel)) match {
        case Success(s) ⇒ logger.debug("running  ChannelUpdaterTask OK")
        case Failure(e) ⇒ logger.error("Error: " + e)
      })
  }
  def run = sjxTask.run
}
object ConnectionsControllerStore {
  private[controller] var controller: ConnControlerModelUpdater = null
}
@sfxml
class ConnectionsController(val connTable: TableView[ModelConnection], val chanTable: TableView[ChannelModel]) extends ConnControlerModelUpdater with Logging {

  val buffer = List[ModelConnection]()
  val obuf = ObservableBuffer[ModelConnection](buffer)
  val chanbuffer = List[ChannelModel]()
  val chanOBuf = ObservableBuffer[ChannelModel](chanbuffer)

  MainActor.startConnections
  connTable.columns ++= ModelConnection.tableColumns
  connTable.items = obuf
  // Add selection listener
  connTable.getSelectionModel.selectedItemProperty.addListener(new ChangeListener[ModelConnection] {
    def changed(observable: ObservableValue[_ <: ModelConnection], oldValue: ModelConnection, newValue: ModelConnection): Unit = {
      if (newValue != null) ConnectionsControllerActor.setFilterChannel(true, newValue.pame.value)
      else ConnectionsControllerActor.setFilterChannel(false, "")
      new ChannelUpdaterTask().run
    }
  })
  ConnectionsControllerStore.controller = this
  chanTable.columns ++= ChannelModel.tableColumns
  chanTable.items = chanOBuf
  def updateModel(model: List[ModelConnection]): Unit = {
    obuf.clear
    obuf ++= model
  }
  def updateChannModel(model: List[ChannelModel]): Unit = {
    chanOBuf.clear
    chanOBuf ++= model
  }
}

