/**
 * P19 (**) Rotate a list
N
N places to the left.
Examples:

scala> rotate(3, List('a, 'b, 'c, 'd, 'e, 'f, 'g, 'h, 'i, 'j, 'k))
res0: List[Symbol] = List('d, 'e, 'f, 'g, 'h, 'i, 'j, 'k, 'a, 'b, 'c)

scala> rotate(-2, List('a, 'b, 'c, 'd, 'e, 'f, 'g, 'h, 'i, 'j, 'k))
res1: List[Symbol] = List('j, 'k, 'a, 'b, 'c, 'd, 'e, 'f, 'g, 'h, 'i)
 */
object Q17_RotateNPlacesLeft extends App{

  val myList = List('a','b','c','d','e','f','g','h','i','j','k')

  def rotateNplacesLeft[A](n:Int, ls:List[A]):List[A] = {
    val post = ls.take(n)
    val pre = ls.drop(n)
    pre ::: post
  }
  //adjust the logic for minus values

  println(rotateNplacesLeft(2,myList))
}
