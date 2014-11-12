package rabbit.client

import org.sarrufat.rabbitmq.json.Overview
import org.specs2.mutable.Specification
import akka.actor.ActorSystem
import spray.client.pipelining._
import spray.httpx.SprayJsonSupport
import scala.util.Success
import org.specs2.control.Debug
import scala.util.Failure
import spray.http.BasicHttpCredentials
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.Promise
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class OverviewSpec extends Specification with Debug {

  case class Error(msg: String) extends Exception(msg)
  implicit val system = ActorSystem("simple-spray-client")
  import system.dispatcher // execution context for futures below
  import org.sarrufat.rabbitmq.json.OverviewProtocol._
  import SprayJsonSupport._

  val pipeline = sendReceive ~> unmarshal[Overview]
  val credentials = BasicHttpCredentials("restUser", "restUser")

  lazy val responseFuture = pipeline {
    Get("http://srv-sap-ewmd:15672/api/overview") ~> addCredentials(credentials)
  }
  "Response Future has" in {

    lazy val retProm = Promise[Overview]()
    responseFuture onComplete {
      case Success(ov: Overview)        ⇒ retProm.success(ov)
      case Success(somethingUnexpected) ⇒ retProm.failure(Error("somethingUnexpected"))
      case Failure(error)               ⇒ retProm.failure(error)
    }
    Await.ready(retProm.future, Duration(60, "sec"))
    val ret = retProm.future.value.get
    "management_version must be 3.3.0" in ret.get.management_version === "3.3.0"
    "rabbitmq_version must be 3.3.0" in ret.get.rabbitmq_version === "3.3.0"
    "statistics_level must be fine" in ret.get.statistics_level === "fine"
    "Exchange types length must be >= 4" in ret.get.exchange_types.length >= 4
    val node = ret.get.node
    s"node $node must not be empty" in !node.isEmpty
  }
}
