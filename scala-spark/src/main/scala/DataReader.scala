import domain.{Drawing, DrawingDF}
import org.apache.spark.sql.{Dataset, SparkSession}

import scala.util.Random

class DataReader(session: SparkSession, path: String) {

  import session.implicits._

  def read: Dataset[DrawingDF] = {
    session.read
      .json(path)
      .as[Drawing]
      .map(_.toDataFrame)
  }

  def split(dataset: Dataset[DrawingDF], ratio: Ratio): Array[Dataset[DrawingDF]] = {
    dataset.randomSplit(ratio, seed = Random.nextLong())
  }

  type SplittedDataset = (Dataset[DrawingDF], Dataset[DrawingDF])
  type Ratio = Array[Double]
}
