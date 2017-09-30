package domain

import org.apache.spark.ml.linalg.Vector

case class DrawingDF(features: Vector, label: Int)

object DrawingDF {

  val Labels: Seq[String] = Seq(
    "cello",
    "clarinet",
    "drums",
    "guitar",
    "harp",
    "piano",
    "saxophone",
    "trombone",
    "trumpet",
    "violin"
  )

  def wordToLabel(word: String): Int = Labels.indexOf(word.toLowerCase)
}
