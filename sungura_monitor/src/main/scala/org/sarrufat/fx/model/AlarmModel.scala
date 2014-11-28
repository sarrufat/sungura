package org.sarrufat.fx.model

import java.util.Date
import java.text.SimpleDateFormat

object AlarmModel {

  def now = {
    val df = new SimpleDateFormat("HH:mm:ss")
    df.format(new Date)
  }
}
class AlarmModel(ex: Throwable) {
  val xmlMessage = { <tr><td>{ AlarmModel.now }</td><td>{ ex.getMessage }</td></tr> }
  override def toString = xmlMessage.toString()
}
