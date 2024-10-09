/**
 *  P05 (*) Reverse a list.
    Example:
    scala> reverse(List(1, 1, 2, 3, 5, 8))
    res0: List[Int] = List(8, 5, 3, 2, 1, 1)
 */

object Reverse extends App{

  val myList = List(8,9,7,6,5,4,3,2,1)
  println(myList.reverse)

  //using recursion
  def recursion[A](ls:List[A]): List[A] = ls match {
    case Nil => Nil
    case h::tail => recursion(tail)::: List(h)
  }
  println(recursion(myList))

  def tailRecursion[A](result: List[A], ls:List[A]): List[A] = ls match {
    case Nil => result
    case h::tail => tailRecursion(h::result,tail)
  }
  println(tailRecursion(Nil,myList))

  //using fold
  println(myList.foldLeft(List[Int]()){(a,b) => b::a})
}
