package org.sarrufat.rabbitmq.actor

//import org.sarrufat.fx.MainUIFX
import org.sarrufat.rabbitmq.json._
import akka.actor.{ ActorSystem, Props, Actor }
import grizzled.slf4j.Logging
import spray.client.pipelining.{ Get, WithTransformation, WithTransformerConcatenation, addCredentials, sendReceive, sendReceive$default$3, unmarshal }
import spray.http.BasicHttpCredentials
import spray.httpx.SprayJsonSupport._
import org.sarrufat.fx.MainUIFX
import scala.concurrent.Promise
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Await
import scala.concurrent.duration._
import org.sarrufat.fx.controller.ConnectionsControllerActor

object ConnectionActor extends Logging {

  private val propsOvervie = Props[OverviewActor]
  implicit val system = ActorSystem.create
  import org.sarrufat.rabbitmq.json.ConnectionsJSONProto._
  import system.dispatcher // execution context for futures below
  private val pipeline = sendReceive ~> unmarshal[Seq[ConnectionsJSON]]
  private val credentials = BasicHttpCredentials(MainUIFX.USER, MainUIFX.PASSWORD)

  private def sendREST = {
    lazy val responseFuture = pipeline {
      Get("http://" + MainUIFX.HOST + ":15672/api/connections") ~> addCredentials(credentials)
    }
    lazy val retProm = Promise[Seq[ConnectionsJSON]]()
    responseFuture onComplete {
      case Success(con: Seq[ConnectionsJSON]) ⇒ retProm.success(con)
      case Success(somethingUnexpected)       ⇒ retProm.failure(Error("somethingUnexpected"))
      case Failure(error)                     ⇒ retProm.failure(error)
    }
    Await.ready(retProm.future, 60 seconds)
    val ret = retProm.future.value.get
    logger.debug("Recibido: " + ret.get)
    ConnectionsControllerActor.sender ! ret.get
  }

}

class ConnectionActor extends Actor {
  def receive = {
    case PulseRequest('Connections) ⇒ ConnectionActor.sendREST
  }
}
