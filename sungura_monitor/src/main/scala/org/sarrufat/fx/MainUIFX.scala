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
import org.sarrufat.rabbitmq.actor.MainActor
import java.lang.System

object MainUIFX extends JFXApp {
  lazy val logger = Logger[this.type]
  val applConf = ConfigFactory.load()
  lazy val HOST = applConf.getString("monitor.host")
  lazy val USER = applConf.getString("monitor.user")
  lazy val PASSWORD = applConf.getString("monitor.password")

  val rootLayout = FXMLView(getClass.getResource("/org/sarrufat/fx/view/RootLayout.fxml"), NoDependencyResolver)

  stage = new JFXApp.PrimaryStage {
    title = "RabbitMQ Monitor (" + HOST + ")"
    scene = new Scene(rootLayout)
  }
  stage.setMaximized(true)
  stage.onCloseRequest = handle {
    MainActor.stopOverview
    System.exit(0)
  }
  MainActor.startPoll
  def title(tit: String) = { stage.title = "RabbitMQ Monitor (" + tit + ")" }
}
