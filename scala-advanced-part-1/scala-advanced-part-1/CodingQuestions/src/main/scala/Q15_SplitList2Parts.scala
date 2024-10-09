import scala.::

/** P17 (*) Split a list into two parts.
     The length of the first part is given.  Use a Tuple for your result.

     Example:
     scala> split(3, List('a, 'b, 'c, 'd, 'e, 'f, 'g, 'h, 'i, 'j, 'k))
     res0: (List[Symbol], List[Symbol]) = (List('a, 'b, 'c),List('d, 'e, 'f, 'g, 'h, 'i, 'j, 'k))*/
object Q15_SplitList2Parts extends App{

  val myList = List('a','b','c','d','e','f','g','h','i','j','k')

  def split[A](n:Int,ls:List[A]):(List[A],List[A]) = {
    (ls.take(n),ls.drop(n))
  }
  println(split(3,myList))

  def splitRecursive[A](n:Int, ls:List[A]):(List[A],List[A]) = (n,ls) match {
    case (0,h::tail) => (Nil,h::tail)
    case (n,h::tail) => {
      val (pre,post) = splitRecursive(n-1,tail)
      (h::pre, post)
    }
  }
  println(splitRecursive(3,myList))
}
