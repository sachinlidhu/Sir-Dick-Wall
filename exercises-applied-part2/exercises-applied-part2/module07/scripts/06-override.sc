abstract class Upper {
  def blip: String
  val blop: String = "blop"
  def op(x: Int, y: Int): Int
}

class Lower extends Upper {
  override def blip: String = "blip"
  override val blop: String = "bloop"
  override def op(x: Int, y: Int): Int = x + y
  def op(x: Double, y: Double): Double = x + y
}

