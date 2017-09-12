package domain

case class Drawing(key_id: String,
                   countrycode: String,
                   timestamp: String,
                   drawing: Array[Array[Array[Double]]],
                   recognized: Boolean,
                   word: String) {
  def simplify: SimplifiedDrawing = {
    SimplifiedDrawing(fromStrokes(drawing), word)
  }

  private def fromStrokes(strokes: Array[Array[Array[Double]]]): Array[Array[Int]] = {
    val a = Array.ofDim[Int](256, 256)

    strokes.foreach(stroke => {
      for ((x, y) <- stroke(0) zip stroke(1)) {
        a(x.toInt)(y.toInt) = 1
      }
    })

    a
  }
}
