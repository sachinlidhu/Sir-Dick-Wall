/**
 * P03 (*) Find the Kth element of a list.
By convention, the first element in the list is element 0.

Example:

scala> nth(2, List(1, 1, 2, 3, 5, 8))
res0: Int = 2
 */

object KthElement extends App{

  val myList = List(1,3,5,4,2,6,8,7)

  def buildIn[A](n:Int, ls: List[A]): A = {
    if(ls.isEmpty || n<0) throw new NoSuchElementException
    else ls(n)
  }
  println(buildIn(3,myList))

  def recursive[A](n:Int, ls:List[A]): A = ls match {
    case h::tail if n == 0 => h
    case _::tail => recursive(n-1,tail)
  }
  println(recursive(3,myList))

  def recursive2[A](n:Int, ls:List[A]): A = (n,ls) match {
    case (0,h::tail) => h
    case (n,h::tail) => recursive2(n-1, tail)
    case (_,Nil) => throw new NoSuchElementException()
  }
  println(recursive2  (3,myList))
 }
