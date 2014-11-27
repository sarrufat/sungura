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

import org.sarrufat.rabbitmq.json.ChannelWrapper

object ConnectionActor extends Logging {

  private val propsOvervie = Props[OverviewActor]
  implicit val system = ActorSystem.create

  import system.dispatcher // execution context for futures below

  private val credentials = BasicHttpCredentials(MainUIFX.USER, MainUIFX.PASSWORD)

  private def sendREST = {
    import org.sarrufat.rabbitmq.json.ConnectionsJSONProto._

    val pipeline = sendReceive ~> unmarshal[Seq[ConnectionsJSON]]

    lazy val responseFuture = pipeline {
      Get("http://" + MainActor.hostName + ":15672/api/connections") ~> addCredentials(credentials)
    }
    lazy val retProm = Promise[Seq[ConnectionsJSON]]()
    responseFuture onComplete {
      case Success(con: Seq[ConnectionsJSON]) ⇒ retProm.success(con)
      case Success(somethingUnexpected)       ⇒ retProm.failure(Error("somethingUnexpected"))
      case Failure(error)                     ⇒ retProm.failure(error)
    }
    retProm.future onComplete {
      case Success(con) ⇒ MainActor.sender ! ConnectionWrapper(con)
      case Failure(f)   ⇒ MainActor.sender ! new Failure(f)
    }

  }

  private def sendChannelREST = {
    import org.sarrufat.rabbitmq.json.ChannelJSONProto._
    val pipeline2 = sendReceive ~> unmarshal[Seq[ChannelJsonObject]]

    lazy val responseFuture = pipeline2 {
      Get("http://" + MainActor.hostName + ":15672/api/channels") ~> addCredentials(credentials)
    }
    lazy val retProm = Promise[Seq[ChannelJsonObject]]()
    responseFuture onComplete {
      case Success(chanSeq: Seq[ChannelJsonObject]) ⇒ retProm.success(chanSeq)
      case Success(somethingUnexpected)             ⇒ retProm.failure(Error("somethingUnexpected"))
      case Failure(error)                           ⇒ retProm.failure(error)
    }
    retProm.future onComplete {
      case Success(chSeq) ⇒ MainActor.sender ! ChannelWrapper(chSeq)
      case Failure(f)     ⇒ MainActor.sender ! new Failure(f)
    }

  }

}

class ConnectionActor extends Actor {
  def receive = {
    case PulseRequest('Connections) ⇒ {
      ConnectionActor.sendREST
      ConnectionActor.sendChannelREST
    }
  }
}
