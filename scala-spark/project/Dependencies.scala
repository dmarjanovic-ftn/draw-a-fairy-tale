import sbt._

object Dependencies {
  lazy val sparkVersion = "2.1.0"
  val sparkCore: ModuleID = "org.apache.spark" % "spark-core_2.11" % sparkVersion % "provided"
  val sparkSQL: ModuleID = "org.apache.spark" % "spark-sql_2.11" % sparkVersion % "provided"
  val sparkMLLib: ModuleID = "org.apache.spark" % "spark-mllib_2.11" % sparkVersion % "provided"

  val logback: ModuleID = "ch.qos.logback" % "logback-classic" % "1.1.8"
  val logbackColorizer: ModuleID = "org.tuxdude.logback.extensions" % "logback-colorizer" % "1.0.1"
}
