import java.io.File
import java.util.{Collections, Random}

import org.apache.spark.SparkConf
import org.apache.spark.api.java.JavaSparkContext
import org.datavec.api.io.filters.BalancedPathFilter
import org.datavec.api.io.labels.ParentPathLabelGenerator
import org.datavec.api.split.{FileSplit, InputSplit}
import org.datavec.image.loader.NativeImageLoader
import org.datavec.image.recordreader.ImageRecordReader
import org.deeplearning4j.api.storage.impl.RemoteUIStatsStorageRouter
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.inputs.InputType
import org.deeplearning4j.nn.conf.layers._
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration, Updater}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.api.IterationListener
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer
import org.deeplearning4j.spark.impl.paramavg.ParameterAveragingTrainingMaster
import org.deeplearning4j.ui.stats.StatsListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object FairytaleCNN {
  val log: Logger = LoggerFactory.getLogger(getClass)

  // train set characteristics
  val numExamples: Int = 200000
  val numLabels: Int = 10
  val batchSize: Int = 1000
  val splitTrainTest: Double = 0.8

  val useSparkLocal: Boolean = true

  // image parameters
  val imgHeight: Int = 28
  val imgWidth: Int = 28
  val imgChannels: Int = 1

  // random setup
  val seed: Long = 42
  val random: Random = new Random(seed)

  // NN params
  val epochs: Int = 4
  val iterations: Int = 1

  def main(args: Array[String]): Unit = {
    new FairytaleCNN().run()
  }

}

class FairytaleCNN {

  import FairytaleCNN._

  def run(): Unit = {

    val sparkConf = new SparkConf
    if (useSparkLocal) {
      sparkConf.setMaster("local[*]")
    }

    sparkConf.setAppName("CNN Draw a fairytale")
    sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    sparkConf.set("spark.kryo.registrator", "org.nd4j.Nd4jRegistrator")

    val sparkContext: JavaSparkContext = new JavaSparkContext(sparkConf)

    val labelMaker = new ParentPathLabelGenerator
    val rootPath = new File(System.getenv("CNN_DATA_ROOT_PATH_URL"))
    val fileSplit = new FileSplit(rootPath, NativeImageLoader.ALLOWED_FORMATS, random)
    val pathFilter = new BalancedPathFilter(random, labelMaker, numExamples, numLabels, batchSize)

    // define train and test split
    val inputSplit: Array[InputSplit] = fileSplit.sample(
      pathFilter,
      numExamples * (1 + splitTrainTest),
      numExamples * (1 - splitTrainTest)
    )
    val trainDataSplit: InputSplit = inputSplit(0)
    val testDataSplit: InputSplit = inputSplit(1)
    log.debug(s"Train: ${trainDataSplit.length}, Test: ${testDataSplit.length}")


    val trainData = prepareReadAndScale(trainDataSplit, labelMaker, batchSize, numLabels)
    val trainDataSpark = sparkContext.parallelize(trainData)

    val tm = new ParameterAveragingTrainingMaster.Builder(4, batchSize)
      .averagingFrequency(5)
      .workerPrefetchNumBatches(2)
      .batchSizePerWorker(batchSize)
      .build

    val nnConf = configureNetwork()
    val sparkNet = new SparkDl4jMultiLayer(sparkContext, nnConf, tm)
    sparkNet.setListeners(Seq[IterationListener](new ScoreIterationListener(1)))

    // send data to GUI
    val remoteUIRouter = new RemoteUIStatsStorageRouter(System.getenv("ND4J_UI_MACHINE_IP_AND_PORT"))
    sparkNet.setListeners(remoteUIRouter, Collections.singletonList(new StatsListener(null)))

    // TODO MultipleEpochIterator
    for (i <- 0 until epochs) {
      sparkNet.fit(trainDataSpark)
      log.info("Completed Epoch {}", i)
    }

    val testData = prepareReadAndScale(testDataSplit, labelMaker, 200, numLabels)
    val testDataSpark = sparkContext.parallelize(testData)

    val labels = trainData.head.getLabelNamesList
    val evaluation: Evaluation = sparkNet.evaluate(testDataSpark, labels)
    log.info(evaluation.stats)

    tm.deleteTempFiles(sparkContext)
  }

  private def prepareReadAndScale(data: InputSplit,
                                  labelMaker: ParentPathLabelGenerator,
                                  batchSize: Int,
                                  numLabels: Int): ArrayBuffer[DataSet] = {
    // define default reader and scaler for both sets
    val recordReader = new ImageRecordReader(imgHeight, imgWidth, imgChannels, labelMaker)
    val scaler = new ImagePreProcessingScaler(0, 1)

    recordReader.initialize(data, null)

    val recordIter =
      new RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels)

    // scale
    scaler.fit(recordIter)
    recordIter.setPreProcessor(scaler)

    val dataList = mutable.ArrayBuffer.empty[DataSet]
    while (recordIter.hasNext) {
      dataList += recordIter.next
    }
    dataList
  }

  private def configureNetwork(): MultiLayerNetwork = {
    val conf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
      .seed(seed)
      .iterations(10)
      .regularization(true)
      .l2(0.0001)
      .activation(Activation.RELU)
      .learningRate(0.01)
      .weightInit(WeightInit.XAVIER)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .updater(Updater.NESTEROVS)
      .momentum(0.9)
      .list()
      .layer(0,
        new ConvolutionLayer.Builder()
          .kernelSize(5, 5)
          .name("cnn-init")
          .nIn(imgChannels)
          .nOut(30)
          .activation(Activation.RELU)
          .biasInit(0)
          .build())
      .layer(1,
        new SubsamplingLayer.Builder()
          .kernelSize(2, 2)
          .name("max-pool-1")
          .build())
      .layer(2,
        new ConvolutionLayer.Builder()
          .kernelSize(3, 3)
          .name("cnn-3x3")
          .nOut(15)
          .activation(Activation.RELU)
          .build())
      .layer(3,
        new SubsamplingLayer.Builder()
          .kernelSize(2, 2)
          .name("max-pool-2")
          .build())
      .layer(4,
        new DenseLayer.Builder()
          .nOut(128)
          .activation(Activation.RELU)
          .build())
      .layer(5,
        new DenseLayer.Builder()
          .nOut(50)
          .activation(Activation.RELU)
          .build())
      .layer(6,
        new OutputLayer.Builder(
          LossFunctions.LossFunction.RECONSTRUCTION_CROSSENTROPY)
          .nOut(numLabels)
          .activation(Activation.SOFTMAX)
          .build())
      .backprop(true)
      .pretrain(false)
      .setInputType(InputType.convolutional(imgHeight, imgWidth, imgChannels))
      .build()
    new MultiLayerNetwork(conf)
  }
}