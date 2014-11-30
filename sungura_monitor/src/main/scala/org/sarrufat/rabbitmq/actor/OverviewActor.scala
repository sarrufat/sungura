package org.sarrufat.rabbitmq.actor

import scala.concurrent.{ Await, Promise }
import scala.concurrent.duration.{ Duration, DurationInt }
import scala.util.{ Failure, Success }
import org.sarrufat.fx.MainUIFX
import org.sarrufat.rabbitmq.json.{ Overview, OverviewWTS }
import akka.actor.{ Actor, ActorLogging, ActorSystem, Props, actorRef2Scala }
import grizzled.slf4j.Logger
import spray.client.pipelining.{ Get, WithTransformation, WithTransformerConcatenation, addCredentials, sendReceive, sendReceive$default$3, unmarshal }
import spray.http.BasicHttpCredentials
import spray.httpx.SprayJsonSupport._
import grizzled.slf4j.Logging
import org.sarrufat.rabbitmq.json.Overview

case class Error(msg: String) extends Exception(msg)

object OverviewActor extends Logging {
  //  lazy val logger = Logger[this.type]

  private val propsOverviewActor = Props[OverviewActor]
  implicit val system = ActorSystem.create
  //  lazy val sender = system.actorOf(propsOverviewActor)

  import system.dispatcher // execution context for futures below
  import org.sarrufat.rabbitmq.json.OverviewProtocol._
  private val pipeline = sendReceive ~> unmarshal[Overview]
  private val credentials = BasicHttpCredentials(MainUIFX.USER, MainUIFX.PASSWORD)

  private def sendREST = {
    lazy val responseFuture = pipeline {
      Get("http://" + MainActor.hostName + ":15672/api/overview") ~> addCredentials(credentials)
    }
    responseFuture onComplete {
      case Success(ov: Overview)        ⇒ MainActor.sender ! OverviewWTS(ov)
      case Success(somethingUnexpected) ⇒ MainActor.sender ! new Failure(new Exception("somethingUnexpected"))
      case Failure(error)               ⇒ MainActor.sender ! new Failure(error)
    }
  }
}

class MakeRequest
class OverviewActor extends Actor with Logging {

  def receive = {
    case PulseRequest('Overview) ⇒ OverviewActor.sendREST
  }

}
