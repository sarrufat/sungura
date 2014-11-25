package org.sarrufat.rabbitmq.actor

import akka.actor.Actor
import grizzled.slf4j.Logging
import akka.actor.Props
import akka.actor.ActorSystem
import spray.http.BasicHttpCredentials
import org.sarrufat.fx.MainUIFX
import org.sarrufat.rabbitmq.json.QueueJsonObject
import spray.client.pipelining.{ Get, WithTransformation, WithTransformerConcatenation, addCredentials, sendReceive, sendReceive$default$3, unmarshal }
import spray.httpx.SprayJsonSupport._
import scala.concurrent.{ Promise, Await }
import scala.concurrent.duration._
import scala.util.{ Success, Failure }
import org.sarrufat.rabbitmq.json.QueueJsonWrapper

object QueueActor extends Logging {
  private val propsOvervie = Props[QueueActor]
  implicit val system = ActorSystem.create
  import system.dispatcher // execution context for futures below
  private val credentials = BasicHttpCredentials(MainUIFX.USER, MainUIFX.PASSWORD)
  private def sendREST = {
    import org.sarrufat.rabbitmq.json.QueueJsonProto._
    val pipeline = sendReceive ~> unmarshal[Seq[QueueJsonObject]]

    lazy val responseFuture = pipeline {
      Get("http://" + MainUIFX.HOST + ":15672/api/queues") ~> addCredentials(credentials)
    }

    lazy val retProm = Promise[Seq[QueueJsonObject]]()
    responseFuture onComplete {
      case Success(con: Seq[QueueJsonObject]) ⇒ retProm.success(con)
      case Success(somethingUnexpected)       ⇒ retProm.failure(Error("somethingUnexpected"))
      case Failure(error)                     ⇒ retProm.failure(error)
    }
    Await.ready(retProm.future, 60 seconds)
    val ret = retProm.future.value.get
    logger.debug("Recibido: " + ret.get)
    QueueJsonWrapper(ret.get)
  }
}
class QueueActor extends Actor {
  def receive = {
    case PulseRequest(x) ⇒ context.parent ! QueueActor.sendREST
  }
}
