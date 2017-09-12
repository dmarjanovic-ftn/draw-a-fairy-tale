package domain

import org.apache.spark.ml.linalg.Vector

case class DrawingDF(features: Vector, label: Int)

object DrawingDF {

  val Labels: Seq[String] = Seq(
    "apple",
    "bat",
    "clock",
    "crown",
    "face",
    "lion"
  )

  def wordToLabel(word: String): Int = Labels.indexOf(word.toLowerCase)
}
