package org.sarrufat.fx.model

import java.util.Date

class AlarmModel(ex: Throwable) {
  val xmlMessage = { <tr><td>{ new Date }</td><td>{ ex.getMessage }</td></tr> }
  override def toString = xmlMessage.toString()
}
