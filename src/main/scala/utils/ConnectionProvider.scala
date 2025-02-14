package utils

import immortan.ConnectionProvider
import java.net.{InetSocketAddress, Socket}
import requests.get

class RequestsConnectionProvider extends ConnectionProvider {
  override val proxyAddress: Option[InetSocketAddress] = Option.empty
  override def doWhenReady(action: => Unit): Unit = action
  override def getSocket: Socket = new Socket
  override def get(url: String): String = requests.get(url).text()
}
