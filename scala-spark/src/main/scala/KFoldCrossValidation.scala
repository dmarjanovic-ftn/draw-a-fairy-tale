import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

class KFoldCrossValidation(k: Int, path: String, session: SparkSession) {

  private val logger = LoggerFactory.getLogger(classOf[KFoldCrossValidation])

  private def train(index: Int): String = s"${path}train_$index.json"
  private def validation(index: Int): String = s"${path}validation_$index.json"

  def evaluateWith(models: Seq[Array[Int]]): Seq[Double] = {
    models.map(layers => {
      logger.info(s"Evaluating for ${layers.toList}")
      val accuracies = (0 until k).map(index => {
        val trainSet = new DataReader(session, train(index)).read
        val validationSet = new DataReader(session, validation(index)).read

        val nn = new NeuralNetwork(
          layers = layers,
          maxIterations = 100
        )

        val model: MultilayerPerceptronClassificationModel = nn.fit(trainSet)

        // Predictions on validation dataset
        val predictions = nn.predictOnModel(model, validationSet)

        // Evaluate results
        val accuracy = nn.evaluateWith(NeuralNetwork.MetricAccuracy, predictions)

        logger.info(s"Accuracy for step $index is $accuracy")
        accuracy
      })

      accuracies.sum / accuracies.size
    })
  }

}
