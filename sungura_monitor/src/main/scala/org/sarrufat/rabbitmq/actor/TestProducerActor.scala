package org.sarrufat.rabbitmq.actor

import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorSystem
import com.rabbitmq.client.ConnectionFactory
import org.sarrufat.fx.MainUIFX
import com.github.sstone.amqp.Amqp._
import com.github.sstone.amqp.ConnectionOwner
import com.github.sstone.amqp.ChannelOwner
import java.util.concurrent.TimeUnit
import grizzled.slf4j.Logger
import grizzled.slf4j.Logger
import grizzled.slf4j.Logger
import grizzled.slf4j.Logging
import scala.util.Try
import scala.util.Success
import scala.util.Failure

case class StartTest(val numMessages: Int, val messageSize: Int)
object TestProducerActor {
  implicit val system = ActorSystem.create
  private lazy val producer = {
    // create an AMQP connection
    lazy val connFactory = new ConnectionFactory
    connFactory.setUri("amqp://" + MainUIFX.USER + ":" + MainUIFX.PASSWORD + "@" + MainActor.hostName + "/%2F")
    lazy val conn = system.actorOf(ConnectionOwner.props(connFactory, 5 second))
    lazy val producer = ConnectionOwner.createChildActor(conn, ChannelOwner.props())
    // wait till everyone is actually connected to the broker
    waitForConnection(system, conn, producer).await(10, TimeUnit.SECONDS)
    TestConsumerActor.createConsumer
    producer
  }

}
class TestProducerActor extends Actor with Logging {

  def receive = {
    case StartTest(nm, sm) ⇒ doTest(nm, sm)
  }

  private def content(size: Int) = Array.fill(size)('0'.toByte)

  private def doTest(numMessages: Int, messageSize: Int) = {
    TestProducerActor.producer ! DeclareExchange(ExchangeParameters(name = "test_exch", exchangeType = "direct", passive = false, autodelete = true))
    TestProducerActor.producer ! DeclareQueue(QueueParameters(name = "test_queue", passive = false, durable = false, autodelete = true))
    TestProducerActor.producer ! QueueBind(queue = "test_queue", exchange = "test_exch", routing_key = "testKey")
    Try({
      for (nv ← 1 to numMessages)
        TestProducerActor.producer ! Publish("test_exch", "testKey", content(messageSize), properties = None, mandatory = true, immediate = false)
    }) match {
      case Success(s) ⇒ logger.debug("doTest OK")
      case Failure(e) ⇒ logger.error("Error", e)
    }
  }
}
