import domain.Drawing
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

object Main extends App {

  val conf = new SparkConf().setAppName("draw-a-fairy-tale")
  val sc = new SparkContext(conf)

  val sparkSession = SparkSession
    .builder()
    .getOrCreate()

  import sparkSession.implicits._

  val file = sparkSession.read
    .json("/Users/dmarjanovic/Downloads/test.json")
    .as[Drawing]

  file.map(_.simplify)
    .collect()
    .foreach(simplified => {
      simplified.picture.foreach(a => println(a.toList))
    })

//  file.createOrReplaceTempView("drawings")
//
//  sparkSession
//    .sql("SELECT drawing, word FROM drawings WHERE recognized = true")
//    .map(d => s"${d.get(0)}; ${d.get(1)}")
//    .collect()
//    .foreach(println)

  sc.stop()
}
