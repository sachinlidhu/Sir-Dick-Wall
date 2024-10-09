/**
 * P04 (*) Find the number of elements of a list.
Example:

scala> length(List(1, 1, 2, 3, 5, 8))
res0: Int = 6
 */

object NumberOfElements extends App{

  val mylist = List(3,4,2,1,7,8,45,6,2)

  //using builtin
  println(mylist.length)

  //using foldLeft
  println(mylist.foldLeft(0){(x,_) => x+1})

  //using recursion
  def findLength[A](ls:List[A]): Int = ls match {
    case Nil => 0
    case _::tail => 1 + findLength(tail)
  }
  println(findLength(mylist))

  //using tailrecursion
  def findLengthTailRec[A](res: Int, ls:List[A]): Int = ls match {
    case Nil => res
    case _::tail => findLengthTailRec(res+1, tail)
  }
  println(findLengthTailRec(0,mylist))

}

