import sbtsparksubmit.SparkSubmitPlugin.autoImport._

object SparkSubmit {
  lazy val settings =
    SparkSubmitSetting("spark",
      Seq(
        "--class", "Main",
        "--master", System.getenv("SPARK_MASTER_URL")
      )
    )
}
