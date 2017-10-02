import domain.DrawingDF.Labels
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory

object Main extends App {

  private val logger = LoggerFactory.getLogger(Main.getClass.getSimpleName)

  val conf = new SparkConf().setAppName("draw-a-fairy-tale")
  val sc = new SparkContext(conf)

  val sparkSession = SparkSession
    .builder()
    .getOrCreate()

    val models = Seq(
      Array(784, 200, 100, 30, Labels.size),
      Array(784, 500, 200, 100, Labels.size),
      Array(784, 300, 100, 50, Labels.size),
      Array(784, 200, 100, Labels.size),
      Array(784, 400, 200, Labels.size)
    )

    val kFoldCrossValidator = new KFoldCrossValidation(
      k = 10,
      path = "/Users/dmarjanovic/Desktop/draw-a-fairy-tale/python-preprocessing/data/k-fold/",
      session = sparkSession
    )

    val result = kFoldCrossValidator.evaluateWith(models)
    logger.info(s"Results gained by cross-validation: $result")

    val bestModelIndex = result.indices.maxBy(result)
    val bestModel = models(bestModelIndex)
    logger.info(s"Best model is $bestModel")

  // Read train set along with validation set
  val trainSet = new DataReader(
    session = sparkSession,
    path = s"/Users/dmarjanovic/Desktop/draw-a-fairy-tale/python-preprocessing/data/k-fold/*_0.json"
  ).read

  // Train neural network
  val nn = new NeuralNetwork(
    layers = bestModel,
    maxIterations = 100
  )

  val model: MultilayerPerceptronClassificationModel = nn.fit(trainSet)

  // Read test set
  val testSet = new DataReader(
    session = sparkSession,
    path = "/Users/dmarjanovic/Desktop/draw-a-fairy-tale/python-preprocessing/data/processed/test.json"
  ).read

  // Predictions on test dataset
  val predictions = nn.predictOnModel(model, testSet)

  // Evaluate results
  val accuracy = nn.evaluateWith(NeuralNetwork.MetricAccuracy, predictions)
  println(s"Accuracy on test set is $accuracy")
  val recall = nn.evaluateWith(NeuralNetwork.MetricRecall, predictions)
  println(s"Recall on test set is $recall")
  val f1 = nn.evaluateWith(NeuralNetwork.MetricFMeasure, predictions)
  println(s"F1 measure on test set is $f1")

  sc.stop()
}
