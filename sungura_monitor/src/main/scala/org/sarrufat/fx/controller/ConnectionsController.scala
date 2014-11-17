package org.sarrufat.fx.controller

import scala.util._
import org.sarrufat.rabbitmq.actor.MainActor
import scalafxml.core.macros.sfxml
import scalafx.scene.control.TableView
import org.sarrufat.fx.model.ModelConnection
import scalafx.collections.ObservableBuffer
import grizzled.slf4j.Logging
import scalafx.concurrent.Task
import scalafx.application.Platform

class ConnectionsUpdaterTask extends Logging {

  private val sjxTask = Task[Unit] {

    Platform.runLater(
      Try(ConnectionsControllerStore.controller.updateModel(ConnectionsControllerActor.getModel)) match {
        case Success(s) ⇒ logger.debug("running  ConnectionsUpdaterTask OK")
        case Failure(e) ⇒ logger.error("Error: " + e)
      })
    "Resultado"
  }
  def run = sjxTask.run
}
object ConnectionsControllerStore {
  private[controller] var controller: ConnControlerModelUpdater = null
}
@sfxml
class ConnectionsController(val connTable: TableView[ModelConnection]) extends ConnControlerModelUpdater with Logging {

  val buffer = List[ModelConnection]()
  val obuf = ObservableBuffer[ModelConnection](buffer)
  MainActor.startConnections
  connTable.columns ++= ModelConnection.tableColumns
  connTable.items = obuf
  logger.debug("ConnectionsController init")
  ConnectionsControllerStore.controller = this
  def updateModel(model: List[ModelConnection]): Unit = {
    obuf.clear()
    obuf ++= model
  }

}
