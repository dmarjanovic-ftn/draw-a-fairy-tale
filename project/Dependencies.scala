import sbt._

object Dependencies {
  lazy val sparkVersion = "2.1.0"
  val sparkCore = "org.apache.spark" % "spark-core_2.11" % sparkVersion
}