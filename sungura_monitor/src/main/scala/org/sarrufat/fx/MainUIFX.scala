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

case class TestDependency(val initialPath: String)

object MainUIFX extends JFXApp {
  lazy val logger = Logger[this.type]
  val applConf = ConfigFactory.load()
  val rootLayout = FXMLView(getClass.getResource("/org/sarrufat/fx/view/RootLayout.fxml"), new DependenciesByType(Map(typeOf[TestDependency] -> new TestDependency("hello world"))))

  stage = new JFXApp.PrimaryStage {
    title = "RabbitMQ Monitor"
    scene = new Scene(rootLayout)
  }
  OverviewActor.startPoll
}
