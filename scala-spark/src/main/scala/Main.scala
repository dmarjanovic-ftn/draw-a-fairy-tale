import domain.DrawingDF
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel
import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

import DrawingDF.Labels

object Main extends App {

  val conf = new SparkConf().setAppName("draw-a-fairy-tale")
  val sc = new SparkContext(conf)

  val sparkSession = SparkSession
    .builder()
    .getOrCreate()

  val reader = new DataReader(
    session = sparkSession,
    path = "/Users/dmarjanovic/Desktop/draw-a-fairy-tale/python-preprocessing/data/processed/*.json"
  )

  val data: Dataset[DrawingDF] = reader.read
  val (train, test) = reader.split(data, Array(0.8, 0.2))

  val nn = new NeuralNetwork(
    layers = Array(784, 56, 32, 12, Labels.size),
    maxIterations = 100
  )

  val model: MultilayerPerceptronClassificationModel = nn.fit(train)

  // Predictions on test dataset
  val predictions = nn.predictOnModel(model, train)

  // Evaluate results
  val accuracy = nn.evaluateWith(NeuralNetwork.MetricAccuracy, predictions)

  println(s"Precision: $accuracy")

  sc.stop()
}
