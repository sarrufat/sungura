package org.sarrufat.fx.controller

import org.sarrufat.fx.controller.util.RunLaterTask
import org.sarrufat.fx.model.ExchangeModel
import org.sarrufat.rabbitmq.actor.{ MainActor, ResetModel }
import org.sarrufat.rabbitmq.json.ExchangeWrapper

import akka.actor.Actor
import akka.agent.Agent
import grizzled.slf4j.Logging
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.TableView
import scalafxml.core.macros.sfxml

class ExchangeUpdaterTask extends Logging {
  new RunLaterTask(ExchangeControllerStore.controller.updateModel)

}
trait ExchangeUpdater {
  def updateModel: Unit
  def getModel = ExchangeControllerActor.modelAgent get
}

object ExchangeControllerStore {
  private[controller] var controller: ExchangeUpdater = null
}

object ExchangeControllerActor extends Logging {
  //  lazy val sender = ActorSystem().actorOf(Props[ExchangeControllerActor])
  import scala.concurrent.ExecutionContext.Implicits.global

  private[controller] val modelAgent = Agent(List[ExchangeModel]())
  private def updateModel(msg: ExchangeWrapper) {
    logger.debug("Recibido: " + msg)
    val newModel = msg.seq.map { ex ⇒ new ExchangeModel(ex) }
    modelAgent send newModel.toList
    new ExchangeUpdaterTask
  }
  private def resetModel = {
    modelAgent send List[ExchangeModel]()
    new ExchangeUpdaterTask
  }
}

class ExchangeControllerActor extends Actor with Logging {
  def receive = {
    case msg: ExchangeWrapper ⇒ ExchangeControllerActor updateModel (msg)
    case ResetModel           ⇒ ExchangeControllerActor resetModel
  }
}
@sfxml
class ExchangeController(val exchangeTab: TableView[ExchangeModel]) extends ExchangeUpdater with Logging {

  exchangeTab.columns ++= ExchangeModel.tableColumns
  ExchangeControllerStore.controller = this

  private val buffer = List[ExchangeModel]()
  private val obuf = ObservableBuffer[ExchangeModel](buffer)
  exchangeTab.items = obuf
  MainActor.startExchange
  def updateModel = {
    obuf.clear
    obuf ++= getModel
  }
}
