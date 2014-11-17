package org.sarrufat.fx.model

import scalafx.beans.property.StringProperty
import scalafx.scene.control.TableColumn
import org.sarrufat.rabbitmq.json.ConnectionsJSON
import scalafx.scene.control.TableCell
import scalafx.scene.layout.HBox
import scalafx.scene.layout.HBox
import scalafx.scene.image.ImageView
import scalafx.scene.control.Label
import javafx.geometry.Pos
import scalafx.scene.image.Image

object LocalImage {
  private[model] lazy val redImage = new Image("/images/S_S_LEDR.png")
  private[model] lazy val greenImage = new Image("/images/S_S_LEDG.png")

}
class StateTableCell extends TableCell[ModelConnection, String] {
  private val hBox = new HBox
  private val image = new ImageView
  private val tLab = new Label
  hBox.setAlignment(Pos.CENTER)
  hBox.content.add(image)
  hBox.content.add(tLab)
  delegate.setGraphic(hBox)
  item.onChange { (_, _, newVal) ⇒
    tLab.text = newVal
    newVal match {
      case "running" ⇒ image.image = LocalImage.greenImage
      case null | "" ⇒
      case _         ⇒ image.image = LocalImage.redImage
    }
  }

}
object ModelConnection {
  private val colnames = List("Name", "Protocol", "Client", "From Client", "To Client", "Timeout", "Channels", "User Name", "State")
  private val _tableColumns = List(
    new TableColumn[ModelConnection, String] { cellValueFactory = { _.value.pame } },
    new TableColumn[ModelConnection, String] { cellValueFactory = { _.value.protocol } },
    new TableColumn[ModelConnection, String] { cellValueFactory = { _.value.pclient } },
    new TableColumn[ModelConnection, String] { cellValueFactory = { _.value.pfclient } },
    new TableColumn[ModelConnection, String] { cellValueFactory = { _.value.ptclient } },
    new TableColumn[ModelConnection, String] { cellValueFactory = { _.value.ptout } },
    new TableColumn[ModelConnection, String] { cellValueFactory = { _.value.pchannels } },
    new TableColumn[ModelConnection, String] { cellValueFactory = { _.value.puname } },
    new TableColumn[ModelConnection, String] {
      cellValueFactory = { _.value.pstate }
      cellFactory = { c ⇒
        new StateTableCell
      }
    })
  def tableColumns = {
    colnames zip _tableColumns foreach (tup ⇒ tup._2.text = tup._1)
    _tableColumns.map(tc ⇒ tc.delegate)
  }
  private def clientFormat(msg: ConnectionsJSON) = msg.client_properties.product + "/" + msg.client_properties.platform + " " + msg.client_properties.version

  private def formatQuantOctets(qo: Long) = {
    qo match {
      case _ if (qo <= 1024)                      ⇒ qo.toString + "B"
      case _ if (qo <= 1024 * 1024)               ⇒ qo / 1024 + "kB"
      case _ if (qo <= 1024 * 1024 * 1024)        ⇒ qo / (1024 * 1024) + "MB"
      case _ if (qo <= 1024 * 1024 * 1024 * 1024) ⇒ qo / (1024 * 1024 * 1024) + "GB"
      case _                                      ⇒ qo / (1024 * 1024 * 1024 * 2014) + "TB"

    }
  }
  private def fromClient(msg: ConnectionsJSON) = formatQuantOctets(msg.recv_oct_details.rate.toLong) + "/s " + formatQuantOctets(msg.recv_oct)
  private def toClient(msg: ConnectionsJSON) = formatQuantOctets(msg.send_oct_details.rate.toLong) + "/s " + formatQuantOctets(msg.send_oct)
  private def tout(msg: ConnectionsJSON) = msg.timeout + "s"

}
class ModelConnection(name: String, proto: String, client: String, fclient: String, tclient: String, tout: String, channels: String, uname: String, state: String) {
  val pame = new StringProperty(this, "name", name)
  val protocol = new StringProperty(this, "protocol", proto)
  val pclient = new StringProperty(this, "client", client)
  val pfclient = new StringProperty(this, "fclient", fclient)
  val ptclient = new StringProperty(this, "tclient", tclient)
  val ptout = new StringProperty(this, "tout", tout)
  val pchannels = new StringProperty(this, "channels", channels)
  val puname = new StringProperty(this, "uname", uname)
  val pstate = new StringProperty(this, "state", state)
  def this(msg: ConnectionsJSON) = this(msg.name, msg.protocol, ModelConnection.clientFormat(msg), ModelConnection.fromClient(msg), ModelConnection.toClient(msg), ModelConnection.tout(msg), msg.channels.toString, msg.user, msg.state)
}

