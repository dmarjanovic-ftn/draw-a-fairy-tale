import sbt._

object Dependencies {
  lazy val sparkVersion = "2.1.0"
  val sparkCore = "org.apache.spark" % "spark-core_2.11" % sparkVersion

  val logback = "ch.qos.logback" % "logback-classic" % "1.1.8"
  val logbackColorizer = "org.tuxdude.logback.extensions" % "logback-colorizer" % "1.0.1"
}
