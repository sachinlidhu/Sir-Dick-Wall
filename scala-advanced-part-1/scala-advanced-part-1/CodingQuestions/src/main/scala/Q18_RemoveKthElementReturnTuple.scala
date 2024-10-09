/**
 * P20 (*) Remove the Kth element from a list.
Return the list and the removed element in a Tuple.  Elements are numbered from 0.

Example:

scala> removeAt(1, List('a, 'b, 'c, 'd))
res0: (List[Symbol], Symbol) = (List('a, 'c, 'd),'b)
 */
object Q18_RemoveKthElementReturnTuple extends App{

  val myList = List('a','b','c','d')

  println(myList.splitAt(1))

  def remove[A](n:Int, ls:List[A]):(List[A],A) = (n,ls) match {
    case (0,h::tail) => (tail,h)
    case (n, h::tail) => {
      val (pre,post) = remove(n-1,tail)
      (h::pre, post)
    }
  }
  println(remove(1,myList))

  def removeBuiltIn[A](n:Int, ls:List[A]):(List[A],A) = ls.splitAt(n) match {
    case (pre,h::tail) => (pre ::: tail,h)
  }
  println(removeBuiltIn(1,myList))
}
