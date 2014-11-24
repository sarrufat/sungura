package org.sarrufat.fx.controller

import grizzled.slf4j.Logging
import scalafx.scene.control.TableView
import org.sarrufat.fx.model.ExchangeModel
import scalafxml.core.macros.sfxml
import akka.agent.Agent
import scalafx.collections.ObservableBuffer
import grizzled.slf4j.Logging
import org.sarrufat.rabbitmq.json.ExchangeWrapper
import scalafx.concurrent.Task
import scalafx.application.Platform
import scala.util.Success
import scala.util.Failure
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import org.sarrufat.rabbitmq.json.ExchangeWrapper
import org.sarrufat.fx.model.ExchangeModel
import grizzled.slf4j.Logging
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import org.sarrufat.rabbitmq.actor.MainActor

class ExchangeUpdaterTask extends Logging {
  private val sjxTask = Task[Unit] {
    Platform.runLater(
      Try(ExchangeControllerStore.controller.updateModel) match {
        case Success(s) ⇒ logger.debug("running  ExchangeUpdaterTask OK")
        case Failure(e) ⇒ logger.error("Error: " + e)
      })
  }
  def run = sjxTask.run
}
trait ExchangeUpdater {
  def updateModel: Unit
  def getModel = ExchangeControllerActor.modelAgent get
}

object ExchangeControllerStore {
  private[controller] var controller: ExchangeUpdater = null
}

object ExchangeControllerActor extends Logging {
  lazy val sender = ActorSystem().actorOf(Props[ExchangeControllerActor])
  import scala.concurrent.ExecutionContext.Implicits.global

  private[controller] val modelAgent = Agent(List[ExchangeModel]())
  private def updateModel(msg: ExchangeWrapper) {
    logger.debug("Recibido: " + msg)
    val newModel = msg.seq.map { ex ⇒ new ExchangeModel(ex) }
    modelAgent send newModel.toList
    new ExchangeUpdaterTask().run
  }

}

class ExchangeControllerActor extends Actor with Logging {
  def receive = {
    case msg: ExchangeWrapper ⇒ ExchangeControllerActor updateModel (msg)
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
