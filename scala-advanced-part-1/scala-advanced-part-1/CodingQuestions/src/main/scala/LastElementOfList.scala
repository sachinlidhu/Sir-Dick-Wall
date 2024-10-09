/**
 * P01 (*) Find the last element of a list.
Example:

scala> last(List(1, 1, 2, 3, 5, 8))
res0: Int = 8
 */

object LastElementOfList extends App{

  val elem:List[Int] = List(1,2,3,4,5)
  def lastElement[A](ls: List[A]):A = ls.last

  println(lastElement(elem))

  def recursiveLastElem[A](ls: List[A]): A = ls match {
    case h::Nil => h
    case _::tail => recursiveLastElem(tail)
    case _ => throw new NoSuchElementException
  }
}
