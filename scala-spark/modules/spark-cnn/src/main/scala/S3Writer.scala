import java.io.File

import org.apache.spark.SparkConf
import org.apache.spark.api.java.JavaSparkContext
import org.datavec.api.io.filters.BalancedPathFilter
import org.datavec.api.io.labels.ParentPathLabelGenerator
import org.datavec.api.split.{FileSplit, InputSplit}
import org.datavec.image.loader.NativeImageLoader

object S3Writer extends App {
  import Configuration._
  log.info("S3 Writer")

  val sparkConf = new SparkConf
  sparkConf.setMaster(System.getenv("SPARK_MASTER_URL"))

  sparkConf.setAppName("CNN Draw a fairytale")
  sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
  sparkConf.set("spark.kryo.registrator", "org.nd4j.Nd4jRegistrator")

  val sparkContext: JavaSparkContext = new JavaSparkContext(sparkConf)
  sparkContext.hadoopConfiguration.set("fs.s3n.impl", "org.apache.hadoop.fs.s3native.NativeS3FileSystem")
  sparkContext.hadoopConfiguration.set("fs.s3n.awsAccessKeyId", System.getenv("S3_ACCESS_ID"))
  sparkContext.hadoopConfiguration.set("fs.s3n.awsSecretAccessKey", System.getenv("S3_ACCESS_KEY"))

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

  // save train
  val trainData = prepareReadAndScale(trainDataSplit, labelMaker, BatchSize, NumLabels)
  val trainDataSpark = sparkContext.parallelize(trainData)
  trainDataSpark.saveAsObjectFile("s3n://daft-rdd/train_data")

  // save test
  val testData = prepareReadAndScale(testDataSplit, labelMaker, BatchSize, NumLabels)
  val testDataSpark = sparkContext.parallelize(testData)
  testDataSpark.saveAsObjectFile("s3n://daft-rdd/test_data")

  log.info("S3 Writer finished!")
}
