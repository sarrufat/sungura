package org.sarrufat.rabbitmq.actor

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorSystem
import scala.concurrent.duration._
import akka.actor.Props
import grizzled.slf4j.Logger
import scala.Enumeration
import akka.agent.Agent
import org.sarrufat.rabbitmq.actor.ConnectionActor
import grizzled.slf4j.Logging
import org.sarrufat.rabbitmq.actor.TestProducerActor

object Command extends Enumeration {
  type CommandT = Value
  val Start, Stop = Value
}
object CActors extends Enumeration {
  type CActor = Value
  val Overview, Connections, Test = Value
}
private[actor] case class PulseRequest(action: Symbol)
private[actor] case class ControlCommand(action: Command.CommandT, actor: CActors.CActor)

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
}
class MainActor extends Actor with Logging {

  lazy val overviewActor = context.actorOf(Props[OverviewActor], "Overview")
  lazy val connectionActor = context.actorOf(Props[ConnectionActor], "Connection")
  lazy val testProdActor = context.actorOf(Props[TestProducerActor], "TestProducer")
  import scala.concurrent.ExecutionContext.Implicits.global
  val overviewStatus = Agent(false)
  val connectionsStatus = Agent(false)

  def receive = {
    case ControlCommand(Command.Start, ac) ⇒ {
      logger.debug("Start: " + ac)
      ac match {
        case CActors.Overview    ⇒ overviewStatus send true
        case CActors.Connections ⇒ connectionsStatus send true
        case CActors.Test        ⇒ testProdActor ! true
      }
    }
    case ControlCommand(Command.Stop, ac) ⇒ {
      logger.debug("Stop: " + ac)
      ac match {
        case CActors.Overview    ⇒ overviewStatus send false
        case CActors.Connections ⇒ connectionsStatus send false
        case CActors.Test        ⇒ testProdActor ! false
      }
    }
    case PulseRequest(_) ⇒ {
      if (overviewStatus get)
        overviewActor ! PulseRequest('Overview)
      if (connectionsStatus get)
        connectionActor ! PulseRequest('Connections)
    }
    case st: StartTest ⇒ {
      testProdActor ! st
      TestConsumerActor.createConsumer
    }
    case _ ⇒ logger.warn("Unknow message")
  }
}
