import java.io.File
import java.util
import java.util.Collections

import org.apache.spark.SparkConf
import org.apache.spark.api.java.{JavaRDD, JavaSparkContext}
import org.datavec.api.io.filters.BalancedPathFilter
import org.datavec.api.io.labels.ParentPathLabelGenerator
import org.datavec.api.split.{FileSplit, InputSplit}
import org.datavec.image.loader.NativeImageLoader
import org.deeplearning4j.api.storage.impl.RemoteUIStatsStorageRouter
import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.spark.api.RDDTrainingApproach
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer
import org.deeplearning4j.spark.impl.paramavg.ParameterAveragingTrainingMaster
import org.deeplearning4j.ui.stats.StatsListener
import org.nd4j.linalg.dataset.DataSet

import scala.collection.JavaConversions._
import scala.collection.mutable

object FairytaleCNN extends App {

  import Configuration._

  log.info("FairyTale ConvNet")

  val sparkConf = new SparkConf
  sparkConf.setMaster(System.getenv("SPARK_MASTER_URL"))


  sparkConf.setAppName("CNN Draw a fairytale")
  sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
  sparkConf.set("spark.kryo.registrator", "org.nd4j.Nd4jRegistrator")
  val sparkContext: JavaSparkContext = new JavaSparkContext(sparkConf)

  val labelMaker = new ParentPathLabelGenerator
  val rootPath = new File(System.getenv("ROOT_DATA_PATH"))
  val fileSplit = new FileSplit(rootPath, NativeImageLoader.ALLOWED_FORMATS, RandomGenerator)
  val pathFilter = new BalancedPathFilter(RandomGenerator, labelMaker, NumExamples, NumLabels, BatchSize)

  // define train and test split
  val inputSplit: Array[InputSplit] = fileSplit.sample(
    pathFilter,
    NumExamples * SplitTrainTest,
    NumExamples * (1 - SplitTrainTest)
  )
  val trainDataSplit: InputSplit = inputSplit(0)
  val testDataSplit: InputSplit = inputSplit(1)
  log.debug(s"Train: ${trainDataSplit.length}, Test: ${testDataSplit.length}")

  val trainData = prepareReadAndScale(trainDataSplit, labelMaker, BatchSize, NumLabels)
  val trainDataSpark = sparkContext.parallelize(trainData)

  val tm = new ParameterAveragingTrainingMaster.Builder(BatchSize)
    .averagingFrequency(2)
    .rddTrainingApproach(RDDTrainingApproach.Direct)
    .workerPrefetchNumBatches(1)
    .batchSizePerWorker(BatchSize)
    .build

  val nnConf = configureNetwork()
  val sparkNet = new SparkDl4jMultiLayer(sparkContext, nnConf, tm)
  sparkNet.setListeners(new ScoreIterationListener(5))

  // send data to GUI
  val remoteUIRouter = new RemoteUIStatsStorageRouter(System.getenv("DL4J_UI_URL"))
  sparkNet.setListeners(remoteUIRouter, Collections.singletonList(new StatsListener(null)))

  for (i <- 0 until Epochs) {
    sparkNet.fit(trainDataSpark)
    log.info("Completed Epoch {}", i)
  }

  val testData: mutable.Seq[DataSet] = prepareReadAndScale(testDataSplit, labelMaker, BatchSize, NumLabels)
  val testDataSpark: JavaRDD[DataSet] = sparkContext.parallelize(testData)

  val labels: util.List[String] = testData.head.getLabelNamesList
  val evaluation: Evaluation = sparkNet.evaluate(testDataSpark, labels)
  log.info(evaluation.stats)

  tm.deleteTempFiles(sparkContext)

}
