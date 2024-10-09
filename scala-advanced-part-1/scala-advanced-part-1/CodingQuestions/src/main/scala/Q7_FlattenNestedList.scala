/**
 * P07 (**) Flatten a nested list structure.
Example:

scala> flatten(List(List(1, 1), 2, List(3, List(5, 8))))
res0: List[Any] = List(1, 1, 2, 3, 5, 8)
 */

object Q7_FlattenNestedList extends App{

  val myList = List(List(1, 1), 2, List(3, List(5, 8)))

  def recursive[A](ls:List[A]): List[A] = ls flatMap{
    case a: List[A] => recursive(a)
    case e => List(e)
  }

  println(recursive(myList))
}
