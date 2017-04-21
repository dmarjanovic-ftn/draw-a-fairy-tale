import Dependencies._

lazy val buildSettings = Seq(
  name := "spark-playground",
  version := "1.0.0",
  scalaVersion := "2.11.8"
)

lazy val coreLibs = Seq(sparkCore)

lazy val root = (project in file("."))
  .settings(buildSettings: _*)
  .settings(
    libraryDependencies ++= coreLibs
  )