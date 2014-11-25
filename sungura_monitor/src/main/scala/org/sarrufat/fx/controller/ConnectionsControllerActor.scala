package org.sarrufat.fx.controller

import org.sarrufat.fx.model.ModelConnection
import akka.actor.Actor
import org.sarrufat.rabbitmq.json.ConnectionsJSON
import org.sarrufat.rabbitmq.json.ChannelJsonObject
import grizzled.slf4j.Logging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.agent.Agent
import org.sarrufat.rabbitmq.json.ConnectionWrapper
import org.sarrufat.rabbitmq.json.ChannelWrapper
import org.sarrufat.rabbitmq.json.ConnectionWrapper
import org.sarrufat.rabbitmq.json.ChannelWrapper
import org.sarrufat.fx.model.ChannelModel
import org.sarrufat.fx.model.ChannelModel

trait ConnControlerModelUpdater {
  def updateModel(model: List[ModelConnection]): Unit
  def updateChannModel(model: List[ChannelModel]): Unit
}
object ConnectionsControllerActor extends Logging {
  //  lazy val sender = ActorSystem().actorOf(Props[ConnectionsControllerActor])
  import scala.concurrent.ExecutionContext.Implicits.global
  private val modelAgent = Agent(List[ModelConnection]())
  private val chanModelAgent = Agent(List[ChannelModel]())
  private val filterChanel = Agent((false, ""))
  private def updateModel(msg: ConnectionWrapper) {
    logger.debug("Recibido: " + msg)
    val newModel = msg.seq.map { cn ⇒ new ModelConnection(cn) }
    modelAgent send newModel.toList
    new ConnectionsUpdaterTask().run
  }
  private def updateChannelModel(msg: ChannelWrapper) {
    val newModel = msg.seq.map { x ⇒ new ChannelModel(x) }
    chanModelAgent send newModel.toList
    new ChannelUpdaterTask().run
  }

  def getModel = modelAgent get
  def getChannModel = {
    filterChanel get match {
      case (true, f) ⇒ { chanModelAgent.get.filter(ch ⇒ ch.pname.value.startsWith(f)) }
      case _         ⇒ chanModelAgent get
    }
  }

  def setFilterChannel(b: Boolean, filter: String) = filterChanel send (b, filter)
  def getFilterChannel = filterChanel get
}
class ConnectionsControllerActor extends Actor with Logging {

  def receive = {
    case Seq()                  ⇒
    case msg: ConnectionWrapper ⇒ ConnectionsControllerActor updateModel (msg)
    case msg: ChannelWrapper    ⇒ ConnectionsControllerActor updateChannelModel (msg)
  }
}
