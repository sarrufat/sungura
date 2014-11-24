package org.sarrufat.rabbitmq.actor

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorSystem
import grizzled.slf4j.Logging

import org.sarrufat.rabbitmq.json._
import org.sarrufat.fx.MainUIFX
import org.sarrufat.fx.controller._
import spray.client.pipelining.{ Get, WithTransformation, WithTransformerConcatenation, addCredentials, sendReceive, sendReceive$default$3, unmarshal }
import spray.http.BasicHttpCredentials
import spray.httpx.SprayJsonSupport._
import scala.concurrent.Promise
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Promise
import scala.concurrent.Await
import scala.concurrent.duration._

object ExchangeActor extends Logging {
  private val propsOvervie = Props[ExchangeActor]
  implicit val system = ActorSystem.create
  import system.dispatcher // execution context for futures below

  private val credentials = BasicHttpCredentials(MainUIFX.USER, MainUIFX.PASSWORD)
  private def sendREST = {
    import org.sarrufat.rabbitmq.json.ExchangeJsonProto._

    val pipeline = sendReceive ~> unmarshal[Seq[ExchangeJsonObject]]

    lazy val responseFuture = pipeline {
      Get("http://" + MainUIFX.HOST + ":15672/api/exchanges") ~> addCredentials(credentials)
    }
    lazy val retProm = Promise[Seq[ExchangeJsonObject]]()
    responseFuture onComplete {
      case Success(con: Seq[ExchangeJsonObject]) ⇒ retProm.success(con)
      case Success(somethingUnexpected)          ⇒ retProm.failure(Error("somethingUnexpected"))
      case Failure(error)                        ⇒ retProm.failure(error)
    }
    Await.ready(retProm.future, 60 seconds)
    val ret = retProm.future.value.get
    logger.debug("Recibido: " + ret.get)
    ExchangeControllerActor.sender ! ExchangeWrapper(ret.get)
    //    ConnectionsControllerActor.sender ! ConnectionWrapper(ret.get)
  }
}
class ExchangeActor extends Actor {
  def receive = {
    case PulseRequest(x) ⇒ ExchangeActor sendREST
  }
}
