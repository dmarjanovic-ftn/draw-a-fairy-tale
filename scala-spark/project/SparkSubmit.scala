import sbtsparksubmit.SparkSubmitPlugin.autoImport._

object SparkSubmit {
  lazy val settings =
    SparkSubmitSetting("spark",
      Seq(
        "--class", "Main",
        "--master", "spark://dmarjanovic-mbp.local:7077"
      )
    )
}
