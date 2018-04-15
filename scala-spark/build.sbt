import Dependencies._
import sbt.Keys._

lazy val buildSettings = Seq(
  name := "Draw a Fairy Tale",
  version := "2.0.0",
  scalaVersion := "2.11.8"
)

lazy val coreLibs = Seq(sparkCore, sparkSQL, sparkMLLib)

lazy val utils = Seq(logback, logbackColorizer)

lazy val root = (project in file("."))
  .settings(buildSettings: _*)
  .settings(SparkSubmit.settings: _*)
  .settings(
    libraryDependencies ++= (coreLibs ++ utils)
      .map(_.exclude("org.slf4j", "slf4j-log4j12"))
  )

lazy val sparkCnn = (project in file("modules/spark-cnn"))
  .settings(buildSettings: _*)
  .settings(SparkSubmit.settings: _*)
  .settings(
    name := "spark-cnn",
    libraryDependencies ++= Seq(
      dl4jCore,
      dl4jSpark,
      datavecSpark,
      nd4jKryo,
      nd4jNativePlatform,
      dl4jUIModel,
      jacksonDatabind,
      jacksonModuleScala,
      logback,
      logbackColorizer).map(_.exclude("org.slf4j", "slf4j-log4j12"))
  )

lazy val dl4jUiServer = (project in file("modules/dl4j-gui-stats"))
  .settings(buildSettings: _*)
  .settings(
    name := "dl4j-gui-stats",
    libraryDependencies ++= Seq(dl4jUI)
  )
