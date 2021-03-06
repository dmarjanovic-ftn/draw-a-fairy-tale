package domain

import org.apache.spark.ml.linalg.Vectors

final case class Drawing(drawing: Array[Double], word: String) {

  def toDataFrame: DrawingDF = DrawingDF(Vectors.dense(drawing), DrawingDF.wordToLabel(word))

}
