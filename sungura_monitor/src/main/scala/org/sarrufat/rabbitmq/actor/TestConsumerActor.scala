package org.sarrufat.rabbitmq.actor
import com.github.sstone.amqp._
import com.github.sstone.amqp.Amqp._
import com.github.sstone.amqp.ChannelOwner
import com.github.sstone.amqp.ConnectionOwner
import akka.actor.ActorSystem
import com.rabbitmq.client.ConnectionFactory
import java.util.concurrent.TimeUnit
import org.sarrufat.fx.MainUIFX
import scala.concurrent.duration._
import com.github.sstone.amqp.Consumer
import akka.actor.{ Actor, Props }
import grizzled.slf4j.Logging

object TestConsumerActor {
  implicit val system = ActorSystem.create
  def createConsumer = {
    // create an AMQP connection
    lazy val connFactory = new ConnectionFactory
    connFactory.setUri("amqp://" + MainUIFX.USER + ":" + MainUIFX.PASSWORD + "@" + MainActor.hostName + "/%2F")
    lazy val conn = system.actorOf(ConnectionOwner.props(connFactory, 5 second))
    lazy val producer = ConnectionOwner.createChildActor(conn, ChannelOwner.props())
    val listener = system.actorOf(Props[TestConsumerActor])
    val queueParams = QueueParameters("my_queue", passive = false, durable = false, exclusive = false, autodelete = true)
    val consumer = ConnectionOwner.createChildActor(conn, Consumer.props(Some(listener), channelParams = None, autoack = false))
    // wait till everyone is actually connected to the broker
    Amqp.waitForConnection(system, consumer).await()
    consumer ! AddQueue(QueueParameters(name = "test_queue", passive = false))
  }
}
class TestConsumerActor extends Actor with Logging {
  def receive = {
    case Delivery(consumerTag, envelope, properties, body) ⇒ {
      //      logger.debug()
      sender ! Ack(envelope.getDeliveryTag)
    }
  }
}
