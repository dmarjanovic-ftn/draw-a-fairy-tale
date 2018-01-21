import domain.DrawingDF.Labels
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory

object Main extends App {

  private val logger = LoggerFactory.getLogger(Main.getClass.getSimpleName)

  val conf = new SparkConf().setAppName("draw-a-fairy-tale")
  val sc = new SparkContext(conf)

  sc.hadoopConfiguration.set("fs.s3n.impl","org.apache.hadoop.fs.s3native.NativeS3FileSystem")
  sc.hadoopConfiguration.set("fs.s3n.awsAccessKeyId", System.getenv("AWS_ACCESS_KEY_ID"))
  sc.hadoopConfiguration.set("fs.s3n.awsSecretAccessKey", System.getenv("AWS_SECRET_ACCESS_KEY"))

  val sparkSession = SparkSession
    .builder()
    .getOrCreate()

    val layers = Array(784, 200, 100, 30, Labels.size)

  // Read train set along with validation set
  val trainSet = new DataReader(
    session = sparkSession,
    path = System.getenv("DATA_PATH_URL")
  ).read

  // Train neural network
  val nn = new NeuralNetwork(
    layers = layers,
    maxIterations = 100
  )

  val model: MultilayerPerceptronClassificationModel = nn.fit(trainSet)
 println(s"Successfully evaluated model $model.")

  sc.stop()
}
