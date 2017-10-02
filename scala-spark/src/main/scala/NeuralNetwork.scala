import domain.DrawingDF
import org.apache.spark.ml.classification.{MultilayerPerceptronClassificationModel, MultilayerPerceptronClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.sql.{DataFrame, Dataset}

import scala.util.Random

class NeuralNetwork(layers: Array[Int], maxIterations: Int, blockSize: Int = 128, seed: Int = Random.nextInt) {

  private val trainer = new MultilayerPerceptronClassifier()
    .setLayers(layers)
    .setBlockSize(blockSize)
    .setSeed(seed)
    .setMaxIter(maxIterations)

  def fit(data: Dataset[DrawingDF]): MultilayerPerceptronClassificationModel = {
    trainer.fit(data)
  }

  def predictOnModel(model: MultilayerPerceptronClassificationModel, input: Dataset[DrawingDF]): DataFrame = {
    model.transform(input).select("prediction", "label")
  }

  def evaluateWith(metric: String, predictions: DataFrame): Double = {
    new MulticlassClassificationEvaluator()
      .setMetricName(metric)
      .evaluate(predictions)
  }

}

object NeuralNetwork {
  val MetricAccuracy: String = "accuracy"
  val MetricFMeasure: String = "f1"
  val MetricRecall: String = "weightedRecall"
}
