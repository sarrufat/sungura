package org.sarrufat.rabbitmq.actor

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorSystem
import akka.actor.Props
import grizzled.slf4j.Logger
import scala.Enumeration
import scala.concurrent.duration._
import akka.agent.Agent
import grizzled.slf4j.Logging
import org.sarrufat.fx.controller.{ OverviewControllerActor, ConnectionsControllerActor, ExchangeControllerActor, QueueControllerActor }
import org.sarrufat.rabbitmq.json._
import org.sarrufat.fx.MainUIFX

object Command extends Enumeration {
  type CommandT = Value
  val Start, Stop = Value
}
object CActors extends Enumeration {
  type CActor = Value
  val Overview, Connections, Test, Exchange, Queue = Value
}
private[actor] case class PulseRequest(action: Symbol)
private[actor] case class ControlCommand(action: Command.CommandT, actor: CActors.CActor)
case object ResetModel

object MainActor {
  implicit val system = ActorSystem.create
  private lazy val mainActorProps = Props[MainActor]
  import system.dispatcher // execution context for futures below
  lazy val sender = system.actorOf(mainActorProps)
  private lazy val cancellable = ActorSystem().scheduler.schedule(5 second, 5 second, sender, PulseRequest('Overview))
  def startPoll = cancellable
  def startOverview = sender ! ControlCommand(Command.Start, CActors.Overview)
  def stopOverview = sender ! ControlCommand(Command.Stop, CActors.Overview)
  def startConnections = sender ! ControlCommand(Command.Start, CActors.Connections)
  def stopConnections = sender ! ControlCommand(Command.Stop, CActors.Connections)
  def startTest(nm: Int, sm: Int) = sender ! StartTest(nm, sm)
  def stopTest = sender ! ControlCommand(Command.Stop, CActors.Test)
  def startExchange = sender ! ControlCommand(Command.Start, CActors.Exchange)
  def stopExchange = sender ! ControlCommand(Command.Stop, CActors.Exchange)
  def startQueue = sender ! ControlCommand(Command.Start, CActors.Queue)
  def stopQueue = sender ! ControlCommand(Command.Stop, CActors.Queue)
  def stopAll = {
    stopOverview
    stopConnections
    stopExchange
    stopQueue
  }
  def startAll = {
    startOverview
    startConnections
    startExchange
    startQueue
  }
  private val hostNameAg = Agent(MainUIFX.HOST)
  def hostName: String = hostNameAg get
  def hostName_=(h: String) = {
    hostNameAg send h
    MainUIFX.title(h)
    sender ! ResetModel
  }
}
class MainActor extends Actor with Logging {

  lazy val overviewActor = context.actorOf(Props[OverviewActor], "Overview")
  lazy val connectionActor = context.actorOf(Props[ConnectionActor], "Connection")
  lazy val testProdActor = context.actorOf(Props[TestProducerActor], "TestProducer")
  lazy val exchangeActor = context.actorOf(Props[ExchangeActor], "ExchangeAct")
  lazy val queueActor = context.actorOf(Props[QueueActor], "QueueActor")

  lazy val ovControllerActor = context.actorOf(Props[OverviewControllerActor], "OverviewControllerActor")
  lazy val connControllerActor = context.actorOf(Props[ConnectionsControllerActor], "ConnectionsControllerActor")
  lazy val exchControllerActor = context.actorOf(Props[ExchangeControllerActor], "ExchangeControllerActor")
  lazy val queueControlleActor = context.actorOf(Props[QueueControllerActor], "QueueControllerActor")
  import scala.concurrent.ExecutionContext.Implicits.global
  val overviewStatus = Agent(false)
  val connectionsStatus = Agent(false)
  val exchagneStatus = Agent(false)
  val queueStatus = Agent(false)

  def receive = {
    case ControlCommand(Command.Start, ac) ⇒ {
      logger.debug("Start: " + ac)
      ac match {
        case CActors.Overview    ⇒ overviewStatus send true
        case CActors.Connections ⇒ connectionsStatus send true
        case CActors.Test        ⇒ testProdActor ! true
        case CActors.Exchange    ⇒ exchagneStatus send true
        case CActors.Queue       ⇒ queueStatus send true
      }
    }
    case ControlCommand(Command.Stop, ac) ⇒ {
      logger.debug("Stop: " + ac)
      ac match {
        case CActors.Overview    ⇒ overviewStatus send false
        case CActors.Connections ⇒ connectionsStatus send false
        case CActors.Test        ⇒ testProdActor ! false
        case CActors.Exchange    ⇒ exchagneStatus send false
        case CActors.Queue       ⇒ queueStatus send false

      }
    }
    case PulseRequest(_) ⇒ {
      if (overviewStatus get)
        overviewActor ! PulseRequest('Overview)
      if (connectionsStatus get)
        connectionActor ! PulseRequest('Connections)
      if (exchagneStatus get)
        exchangeActor ! PulseRequest('Exchange)
      if (queueStatus get)
        queueActor ! PulseRequest('Queue)
    }
    case st: StartTest ⇒ {
      testProdActor ! st
    }
    case ov: OverviewWTS         ⇒ ovControllerActor ! ov
    case conn: ConnectionWrapper ⇒ connControllerActor ! conn
    case chan: ChannelWrapper    ⇒ connControllerActor ! chan
    case exchg: ExchangeWrapper  ⇒ exchControllerActor ! exchg
    case q: QueueJsonWrapper     ⇒ queueControlleActor ! q
    case ResetModel ⇒ {
      ovControllerActor ! ResetModel
      connControllerActor ! ResetModel
      exchControllerActor ! ResetModel
      queueControlleActor ! ResetModel
    }
    case _ ⇒ logger.warn("Unknow message")
  }
}
