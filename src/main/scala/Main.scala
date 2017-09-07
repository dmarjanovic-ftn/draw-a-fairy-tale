import org.apache.spark.{SparkConf, SparkContext}

object Main extends App {

  val conf = new SparkConf().setAppName("draw-a-fairy-tale")
  val sc = new SparkContext(conf)

  val file = sc.textFile("README.md")

  file.collect().foreach(println)

  sc.stop()
}
