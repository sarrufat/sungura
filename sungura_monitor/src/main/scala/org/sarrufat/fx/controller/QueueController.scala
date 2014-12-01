package org.sarrufat.fx.controller

import org.sarrufat.fx.model.QueueModel
import org.sarrufat.rabbitmq.actor.MainActor
import org.sarrufat.rabbitmq.json.QueueJsonWrapper
import akka.actor.Actor
import grizzled.slf4j.Logging
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.TableView
import scalafxml.core.macros.sfxml
import org.sarrufat.rabbitmq.actor.ResetModel
import org.sarrufat.fx.controller.util.RunLaterTask

class QueueControllerActor extends Actor {
  import scala.concurrent.ExecutionContext.Implicits.global
  def receive = {
    case qw: QueueJsonWrapper ⇒ {
      val newModel = qw.seq.map(info ⇒ new QueueModel(info)).toList
      QueueControllerStore.controller.updateModel(newModel)
    }
    case ResetModel ⇒ QueueControllerStore.controller.updateModel(List[QueueModel]())
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
    new RunLaterTask({
      obuf.clear
      obuf ++= model
    })
  }
}
