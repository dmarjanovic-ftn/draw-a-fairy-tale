package domain

import org.apache.spark.ml.linalg.Vector

final case class DrawingDF(features: Vector, label: Int)

object DrawingDF {

  val Labels: Seq[String] = Seq(
    "aircraft_carrier",
    "airplane",
    "alarm_clock",
    "ambulance",
    "angel",
    "animal_migration",
    "ant",
    "anvil",
    "apple",
    "arm",
    "asparagus",
    "axe"
  )

  def wordToLabel(word: String): Int = Labels.indexOf(word.toLowerCase)
}
