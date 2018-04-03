import org.deeplearning4j.ui.api.UIServer

object StatsUIServer extends App {
  val uiServer = UIServer.getInstance
  uiServer.enableRemoteListener()
}
