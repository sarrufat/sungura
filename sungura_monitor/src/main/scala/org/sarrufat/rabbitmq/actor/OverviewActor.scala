package org.sarrufat.rabbitmq.actor

import org.sarrufat.fx.controller.OverviewControllerActor
import org.sarrufat.rabbitmq.json._
import scala.concurrent.duration._
import spray.client.pipelining._
import spray.http.BasicHttpCredentials
import spray.httpx.SprayJsonSupport
import akka.actor.{ Actor, ActorLogging }
import akka.actor.Props
import akka.actor.ActorSystem
import akka.actor.Actor._
import scala.concurrent.Promise
import scala.concurrent.Await
import grizzled.slf4j.Logger
import scala.util.Success
import scala.util.Failure

case class Error(msg: String) extends Exception(msg)

object OverviewActor {
  lazy val logger = Logger[this.type]

  private val propsOverviewActor = Props[OverviewActor]
  implicit val system = ActorSystem.create
  def sender = system.actorOf(propsOverviewActor)

  import system.dispatcher // execution context for futures below
  //  import scala.concurrent.ExecutionContext.Implicits._
  private lazy val cancellable = ActorSystem().scheduler.schedule(0 milliseconds,
    5 second,
    sender,
    new MakeRequest)
  def startPoll = cancellable
  import org.sarrufat.rabbitmq.json.OverviewProtocol._
  import SprayJsonSupport._
  private val pipeline = sendReceive ~> unmarshal[Overview]
  private val credentials = BasicHttpCredentials("restUser", "restUser")

  private def sendREST = {
    lazy val responseFuture = pipeline {
      Get("http://srv-sap-ewmd:15672/api/overview") ~> addCredentials(credentials)
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
