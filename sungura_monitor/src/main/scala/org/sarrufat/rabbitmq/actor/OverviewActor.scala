package org.sarrufat.rabbitmq.actor

import scala.concurrent.{ Await, Promise }
import scala.concurrent.duration.{ Duration, DurationInt }
import scala.util.{ Failure, Success }

import org.sarrufat.fx.MainUIFX
import org.sarrufat.fx.controller.OverviewControllerActor
import org.sarrufat.rabbitmq.json.{ Overview, OverviewWTS }

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props, actorRef2Scala }
import grizzled.slf4j.Logger
import spray.client.pipelining.{ Get, WithTransformation, WithTransformerConcatenation, addCredentials, sendReceive, sendReceive$default$3, unmarshal }
import spray.http.BasicHttpCredentials
import spray.httpx.SprayJsonSupport._

case class Error(msg: String) extends Exception(msg)

object OverviewActor {
  lazy val logger = Logger[this.type]

  private val propsOverviewActor = Props[OverviewActor]
  implicit val system = ActorSystem.create
  lazy val sender = system.actorOf(propsOverviewActor)

  import system.dispatcher // execution context for futures below
  //  import scala.concurrent.ExecutionContext.Implicits._
  private lazy val cancellable = ActorSystem().scheduler.schedule(0 milliseconds,
    5 second,
    sender,
    new MakeRequest)
  def startPoll = cancellable
  import org.sarrufat.rabbitmq.json.OverviewProtocol._
  private val pipeline = sendReceive ~> unmarshal[Overview]
  private val credentials = BasicHttpCredentials(MainUIFX.USER, MainUIFX.PASSWORD)

  private def sendREST = {
    lazy val responseFuture = pipeline {
      Get("http://" + MainUIFX.HOST + ":15672/api/overview") ~> addCredentials(credentials)
    }
    lazy val retProm = Promise[Overview]()
    responseFuture onComplete {
      case Success(ov: Overview)        ⇒ retProm.success(ov)
      case Success(somethingUnexpected) ⇒ retProm.failure(Error("somethingUnexpected"))
      case Failure(error)               ⇒ retProm.failure(error)
    }
    Await.ready(retProm.future, Duration(60, "sec"))
    val ret = retProm.future.value.get
    logger.debug("Recibido: " + ret.get)
    OverviewControllerActor.sender ! OverviewWTS(ret.get)
  }
}

class MakeRequest
class OverviewActor extends Actor with ActorLogging {

  def receive = {
    case mq: MakeRequest ⇒ OverviewActor.sendREST
  }

}
