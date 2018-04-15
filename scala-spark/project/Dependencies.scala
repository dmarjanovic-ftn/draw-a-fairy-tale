import sbt._

object Dependencies {
  lazy val sparkVersion = "2.0.0"
  lazy val dl4jVer = "0.8.0"
  lazy val dl4jVerSpark = "0.8.0_spark_2"

  val sparkCore = "org.apache.spark" % "spark-core_2.11" % sparkVersion % "provided"
  val sparkSQL = "org.apache.spark" % "spark-sql_2.11" % sparkVersion % "provided"
  val sparkMLLib = "org.apache.spark" % "spark-mllib_2.11" % sparkVersion % "provided"

  val logback = "ch.qos.logback" % "logback-classic" % "1.1.8"
  val logbackColorizer = "org.tuxdude.logback.extensions" % "logback-colorizer" % "1.0.1"

  val dl4jCore = "org.deeplearning4j" % "rl4j-core" % dl4jVer
  val datavec = "org.datavec" % "datavec-api" % dl4jVer
  val datavecImage = "org.datavec" % "datavec-data-image" % dl4jVer
  val dl4jSpark = "org.deeplearning4j" %% "dl4j-spark" % dl4jVerSpark
  val datavecSpark = "org.datavec" %% "datavec-spark" % dl4jVerSpark
  val nd4jNativePlatform = "org.nd4j" % "nd4j-native-platform" % dl4jVer
  val nd4jKryo = "org.nd4j" %% "nd4j-kryo" % dl4jVer
  val dl4jUIModel = "org.deeplearning4j" % "deeplearning4j-ui-model" % dl4jVer
  val dl4jUI = "org.deeplearning4j" %% "deeplearning4j-ui" % dl4jVer

  val jacksonDatabind = "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.1"
  val jacksonModuleScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.1"
}
