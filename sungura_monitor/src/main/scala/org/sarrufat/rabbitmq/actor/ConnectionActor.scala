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

    responseFuture onComplete {
      case Success(con: Seq[ConnectionsJSON]) ⇒ MainActor.sender ! ConnectionWrapper(con)
      case Success(somethingUnexpected)       ⇒ MainActor.sender ! new Failure(new Exception("somethingUnexpected"))
      case Failure(error)                     ⇒ MainActor.sender ! new Failure(error)
    }
  }

  private def sendChannelREST = {
    import org.sarrufat.rabbitmq.json.ChannelJSONProto._
    val pipeline2 = sendReceive ~> unmarshal[Seq[ChannelJsonObject]]

    lazy val responseFuture = pipeline2 {
      Get("http://" + MainActor.hostName + ":15672/api/channels") ~> addCredentials(credentials)
    }
    responseFuture onComplete {
      case Success(chanSeq: Seq[ChannelJsonObject]) ⇒MainActor.sender ! ChannelWrapper(chanSeq)
      case Success(somethingUnexpected)             ⇒  MainActor.sender ! new Failure(new Exception("somethingUnexpected"))
      case Failure(error)                           ⇒  MainActor.sender ! new Failure(error)
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
