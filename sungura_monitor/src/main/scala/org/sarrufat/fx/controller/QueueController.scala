package org.sarrufat.fx.controller

import scalafx.scene.control.TableView
import org.sarrufat.fx.model.QueueModel
import org.sarrufat.rabbitmq.actor.MainActor
import org.sarrufat.rabbitmq.json.QueueJsonWrapper
import scalafxml.core.macros.sfxml
import org.sarrufat.fx.model.QueueModel
import scalafx.collections.ObservableBuffer
import scalafx.concurrent.Task
import akka.actor.Actor
import akka.agent.Agent
import scalafx.application.Platform
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import grizzled.slf4j.Logging

class QueueControllerActor extends Actor {
  import scala.concurrent.ExecutionContext.Implicits.global
  def receive = {
    case qw: QueueJsonWrapper ⇒ {
      val newModel = qw.seq.map(info ⇒ new QueueModel(info)).toList
      QueueControllerStore.controller.updateModel(newModel)
    }
  }
}
trait QueueUpdater {
  def updateModel(model: List[QueueModel]): Unit
}
object QueueControllerStore {
  private[controller] var controller: QueueUpdater = null
}
@sfxml
class QueueController(val queueTab: TableView[QueueModel]) extends QueueUpdater with Logging {
  queueTab.columns ++= QueueModel.tableColumns
  private val buffer = List[QueueModel]()
  private val obuf = ObservableBuffer[QueueModel](buffer)
  queueTab.items = obuf
  QueueControllerStore.controller = this
  MainActor.startQueue

  def updateModel(model: List[QueueModel]): Unit = {
    val sjxTask = Task[Unit] {
      Platform.runLater(
        Try({
          obuf.clear
          obuf ++= model
        }) match {
          case Success(s) ⇒ logger.debug("running  updateModel OK")
          case Failure(e) ⇒ logger.error("Error: " + e)
        })
    }
    sjxTask.run
  }
}
