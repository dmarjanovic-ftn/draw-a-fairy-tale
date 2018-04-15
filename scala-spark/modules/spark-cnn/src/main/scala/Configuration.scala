import java.util.Random

import org.datavec.api.io.labels.ParentPathLabelGenerator
import org.datavec.api.split.InputSplit
import org.datavec.image.recordreader.ImageRecordReader
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.inputs.InputType
import org.deeplearning4j.nn.conf.layers._
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration, Updater}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Configuration {
  val log: Logger = LoggerFactory.getLogger(getClass)

  // train set characteristics
  val NumExamples: Int = 240000
  val NumLabels: Int = 48
  val BatchSize: Int = 5000
  val SplitTrainTest: Double = 0.8

  // image parameters
  val ImgHeight: Int = 28
  val ImgWidth: Int = 28
  val ImgChannels: Int = 1

  // random setup
  val Seed: Long = 42
  val RandomGenerator: Random = new Random(Seed)

  // NN params
  val Epochs: Int = 1111
  val Iterations: Int = 1

  def prepareReadAndScale(data: InputSplit,
                          labelMaker: ParentPathLabelGenerator,
                          batchSize: Int,
                          numLabels: Int): ArrayBuffer[DataSet] = {
    // define default reader and scaler for both sets
    val recordReader = new ImageRecordReader(ImgHeight, ImgWidth, ImgChannels, labelMaker)
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

  def configureNetwork(): MultiLayerNetwork = {
    val conf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
      .seed(Seed)
      .regularization(true)
      .l2(0.00005)
      .iterations(Iterations)
      .learningRate(0.008)
      .weightInit(WeightInit.XAVIER)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .updater(Updater.NESTEROVS)
      .momentum(0.9)
      .list()
      .layer(0,
        new ConvolutionLayer.Builder()
          .kernelSize(5, 5)
          .name("cnn-init")
          .nIn(ImgChannels)
          .nOut(30)
          .activation(Activation.RELU)
          .biasInit(0)
          .build())
      .layer(1,
        new SubsamplingLayer.Builder(PoolingType.MAX)
          .kernelSize(2, 2)
          .stride(2, 2)
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
        new SubsamplingLayer.Builder(PoolingType.MAX)
          .kernelSize(2, 2)
          .stride(2, 2)
          .name("max-pool-2")
          .dropOut(0.8)
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
          .nOut(NumLabels)
          .activation(Activation.SOFTMAX)
          .build())
      .backprop(true)
      .pretrain(false)
      .setInputType(InputType.convolutionalFlat(ImgHeight, ImgWidth, ImgChannels))
      .build()
    new MultiLayerNetwork(conf)
  }
}
