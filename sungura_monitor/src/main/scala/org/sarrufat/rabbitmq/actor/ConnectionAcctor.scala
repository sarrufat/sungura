package org.sarrufat.rabbitmq.actor

//import org.sarrufat.fx.MainUIFX
import org.sarrufat.rabbitmq.json._
import akka.actor.{ ActorSystem, Props }
import grizzled.slf4j.Logger
import spray.client.pipelining.{ Get, WithTransformation, WithTransformerConcatenation, addCredentials, sendReceive, sendReceive$default$3, unmarshal }
import spray.http.BasicHttpCredentials
import spray.httpx.SprayJsonSupport._
import org.sarrufat.fx.MainUIFX
import scala.concurrent.Promise
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ConnectionAcctor {
  lazy val logger = Logger[this.type]

  private val propsOverviewActor = Props[OverviewActor]
  implicit val system = ActorSystem.create
  import org.sarrufat.rabbitmq.json.ConnectionsJSONProto._
  import system.dispatcher // execution context for futures below
  private val pipeline = sendReceive ~> unmarshal[ConnectionsJSON]
  private val credentials = BasicHttpCredentials(MainUIFX.USER, MainUIFX.PASSWORD)

  private def sendREST = {
    lazy val responseFuture = pipeline {
      Get("http://" + MainUIFX.HOST + ":15672/api/connections") ~> addCredentials(credentials)
    }
    lazy val retProm = Promise[ConnectionsJSON]()
    responseFuture onComplete {
      case Success(con: ConnectionsJSON) ⇒ retProm.success(con)
      case Success(somethingUnexpected)  ⇒ retProm.failure(Error("somethingUnexpected"))
      case Failure(error)                ⇒ retProm.failure(error)
    }
    Await.ready(retProm.future, Duration(60, "sec"))
    val ret = retProm.future.value.get
    logger.debug("Recibido: " + ret.get)
    //    OverviewControllerActor.sender ! OverviewWTS(ret.get)
  }

}
