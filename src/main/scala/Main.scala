import org.apache.spark.{SparkConf, SparkContext}

object Main extends App {

  println("Hello, bachelor! :)")

  val conf = new SparkConf().setMaster("local").setAppName("spark-playground")
  val sc = new SparkContext(conf)

  val file = sc.textFile("README.md")

  file.foreach(println)

  sc.stop()
}
