import Dependencies._
import sbt.Keys._

lazy val buildSettings = Seq(
  name := "Draw a Fairy Tale",
  version := "1.0.0",
  scalaVersion := "2.11.8"
)

lazy val coreLibs = Seq(sparkCore, sparkSQL)

lazy val utils = Seq(logback, logbackColorizer)

lazy val root = (project in file("."))
  .settings(buildSettings: _*)
  .settings(SparkSubmit.settings: _*)
  .settings(
    libraryDependencies ++= (coreLibs ++ utils)
    .map(_.exclude("org.slf4j", "slf4j-log4j12"))
  )
