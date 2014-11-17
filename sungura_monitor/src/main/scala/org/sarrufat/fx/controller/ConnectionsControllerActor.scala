package org.sarrufat.fx.controller

import org.sarrufat.fx.model.ModelConnection
import akka.actor.Actor
import org.sarrufat.rabbitmq.json.ConnectionsJSON
import grizzled.slf4j.Logging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.agent.Agent
import org.sarrufat.fx.controller.ConnectionsUpdaterTask

trait ConnControlerModelUpdater {
  def updateModel(model: List[ModelConnection]): Unit
}
object ConnectionsControllerActor extends Logging {
  lazy val sender = ActorSystem().actorOf(Props[ConnectionsControllerActor])
  import scala.concurrent.ExecutionContext.Implicits.global
  private val modelAgent = Agent(List[ModelConnection]())
  private def updateModel(msg: Seq[ConnectionsJSON]) {
    logger.debug("Recibido: " + msg)
    val newModel = msg.map { cn ⇒
      new ModelConnection(cn)
    }
    modelAgent send (newModel.toList)
    new ConnectionsUpdaterTask().run
  }
  def getModel = modelAgent get
}
class ConnectionsControllerActor extends Actor with Logging {

  def receive = {
    case Seq()                     ⇒
    case msg: Seq[ConnectionsJSON] ⇒ ConnectionsControllerActor updateModel (msg)
  }
}
