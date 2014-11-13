package org.sarrufat.fx
import grizzled.slf4j.Logger
import scala.concurrent.duration._
import scala.reflect.runtime.universe.typeOf
import scalafx.application.JFXApp
import scalafx.Includes._
import scalafxml.core.FXMLView
import scalafxml.core.DependenciesByType
import scalafx.scene.Scene
import com.typesafe.config.ConfigFactory
import org.sarrufat.rabbitmq.actor.OverviewActor
import org.sarrufat.rabbitmq.json.Overview
import scalafxml.core.NoDependencyResolver

object MainUIFX extends JFXApp {
  lazy val logger = Logger[this.type]
  val applConf = ConfigFactory.load()
  lazy val HOST = applConf.getString("monitor.host")
  lazy val USER = applConf.getString("monitor.user")
  lazy val PASSWORD = applConf.getString("monitor.password")

  val rootLayout = FXMLView(getClass.getResource("/org/sarrufat/fx/view/RootLayout.fxml"), NoDependencyResolver)

  stage = new JFXApp.PrimaryStage {
    title = "RabbitMQ Monitor"
    scene = new Scene(rootLayout)
  }
  OverviewActor.startPoll
}
