package org.sarrufat.fx.controller

import scala.util._
import org.sarrufat.fx.model.{ ChannelModel, ModelConnection }
import org.sarrufat.rabbitmq.actor.MainActor
import grizzled.slf4j.Logging
import javafx.beans.value.{ ChangeListener, ObservableValue }
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.TableView
import scalafxml.core.macros.sfxml
import org.sarrufat.fx.controller.util.RunLaterTask

class ConnectionsUpdaterTask extends Logging {
  new RunLaterTask(ConnectionsControllerStore.controller.updateModel(ConnectionsControllerActor.getModel))
}
class ChannelUpdaterTask extends Logging {
  new RunLaterTask(ConnectionsControllerStore.controller.updateChannModel(ConnectionsControllerActor.getChannModel))
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
      new ChannelUpdaterTask
    }
  })
  ConnectionsControllerStore.controller = this
  chanTable.columns ++= ChannelModel.tableColumns
  chanTable.items = chanOBuf
  def updateModel(model: List[ModelConnection]): Unit = {
    val filterStatus = ConnectionsControllerActor.getFilterChannel
    obuf.clear
    obuf ++= model
    if (filterStatus._1) {
      model.find(_.pame.value == filterStatus._2) match {
        case Some(m) ⇒ connTable.getSelectionModel.select(m)
        case None    ⇒ logger.warn("selection not found: " + filterStatus._2)
      }
    } else
      logger.debug("No selection")
  }
  def updateChannModel(model: List[ChannelModel]): Unit = {
    chanOBuf.clear
    chanOBuf ++= model
  }
}
