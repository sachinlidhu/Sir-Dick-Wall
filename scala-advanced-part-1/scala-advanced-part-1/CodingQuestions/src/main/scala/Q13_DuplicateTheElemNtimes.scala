/*P15 (**) Duplicate the elements of a list a given number of times.
Example:

  scala> duplicateN(3, List('a, 'b, 'c, 'c, 'd))
res0: List[Symbol] = List('a, 'a, 'a, 'b, 'b, 'b, 'c, 'c, 'c, 'c, 'c, 'c, 'd, 'd, 'd)*/
object Q13_DuplicateTheElemNtimes extends App{
  val myList = List('a','b','c','c','d')

  def DuplicateNTimes[A](n:Int,givenList:List[A]):List[A] = (n,givenList) match {
    case (n,_::tail) => givenList.flatMap(x => List.fill(n)(x))
  }
  println(DuplicateNTimes(3,myList))
}
