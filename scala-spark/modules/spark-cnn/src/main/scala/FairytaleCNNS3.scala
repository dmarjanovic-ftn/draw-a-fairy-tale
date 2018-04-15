import java.util.Collections

import org.apache.spark.SparkConf
import org.apache.spark.api.java.{JavaRDD, JavaSparkContext}
import org.deeplearning4j.api.storage.impl.RemoteUIStatsStorageRouter
import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.spark.api.RDDTrainingApproach
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer
import org.deeplearning4j.spark.impl.paramavg.ParameterAveragingTrainingMaster
import org.deeplearning4j.ui.stats.StatsListener
import org.nd4j.linalg.dataset.DataSet

import scala.collection.JavaConversions._


object FairytaleCNNS3 extends App {

  import Configuration._

  log.info("FairyTale ConvNet S3 version.")

  val sparkConf = new SparkConf

  sparkConf.setMaster(System.getenv("SPARK_MASTER_URL"))

  sparkConf.setAppName("CNN Draw a fairytale")
  sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
  sparkConf.set("spark.kryo.registrator", "org.nd4j.Nd4jRegistrator")

  val sparkContext: JavaSparkContext = new JavaSparkContext(sparkConf)
  sparkContext.hadoopConfiguration.set("fs.s3n.impl", "org.apache.hadoop.fs.s3native.NativeS3FileSystem")
  sparkContext.hadoopConfiguration.set("fs.s3n.awsAccessKeyId", System.getenv("S3_ACCESS_ID"))
  sparkContext.hadoopConfiguration.set("fs.s3n.awsSecretAccessKey", System.getenv("S3_ACCESS_KEY"))

  val trainDataSpark: JavaRDD[DataSet] = sparkContext.objectFile[DataSet](System.getenv("S3_TRAIN"))

  val tm = new ParameterAveragingTrainingMaster.Builder(BatchSize)
    .averagingFrequency(4)
    .rddTrainingApproach(RDDTrainingApproach.Direct)
    .workerPrefetchNumBatches(1)
    .batchSizePerWorker(BatchSize)
    .build

  val nnConf = configureNetwork()
  val sparkNet = new SparkDl4jMultiLayer(sparkContext, nnConf, tm)
  sparkNet.setListeners(new ScoreIterationListener(10))

  // send data to GUI
  val remoteUIRouter = new RemoteUIStatsStorageRouter(System.getenv("DL4J_UI_URL"))
  sparkNet.setListeners(remoteUIRouter, Collections.singletonList(new StatsListener(null)))

  for (i <- 0 until Epochs) {
    sparkNet.fit(trainDataSpark)
    log.info("Completed Epoch {}", i)
  }

  val testDataSpark: JavaRDD[DataSet] = sparkContext.objectFile[DataSet](System.getenv("S3_TEST"))

  val labels: java.util.List[String] = Seq[String]("ant", "bat", "bear", "bee", "bird", "butterfly", "camel", "cat", "cow", "crab", "crocodile", "dog", "dolphin", "dragon", "duck", "elephant", "fish", "flamingo", "frog", "giraffe", "hedgehog", "horse", "kangaroo", "lion", "lobster", "monkey", "mosquito", "octopus", "owl", "panda", "parrot", "penguin", "pig", "rabbit", "raccoon", "rhinoceros", "scorpion", "shark", "sheep", "snail", "snake", "spider", "squirrel", "swan", "tiger", "turtle", "whale", "zebra")

  val evaluation: Evaluation = sparkNet.evaluate(testDataSpark, labels)
  log.info(evaluation.stats)

  tm.deleteTempFiles(sparkContext)

}
