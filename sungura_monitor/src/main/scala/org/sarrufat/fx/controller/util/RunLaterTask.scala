package org.sarrufat.fx.controller.util

import scalafx.concurrent.Task
import grizzled.slf4j.Logging
import scalafx.application.Platform
import scala.util.Try
import scala.util.Success
import scala.util.Failure

/**
 *  This class is used for UI views updates
 *
 */
class RunLaterTask(unit: ⇒ Unit) extends Logging {
  private def runLater = {
    Platform.runLater({
      Try(unit) match {
        case Success(s) ⇒ logger.debug("RunLaterTask Success")
        case Failure(f) ⇒ logger.error("RunLaterTask error", f)
      }
    })
  }
  Task[Unit](runLater).run
}
