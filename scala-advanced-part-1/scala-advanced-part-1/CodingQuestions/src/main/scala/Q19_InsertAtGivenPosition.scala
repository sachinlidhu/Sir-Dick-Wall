/**
 * P21 (*) Insert an element at a given position into a list.
Example:

scala> insertAt('new, 1, List('a, 'b, 'c, 'd))
res0: List[Symbol] = List('a, 'new, 'b, 'c, 'd)
 */

object Q19_InsertAtGivenPosition extends App{

  val myList = List('a','b','c','d')

  def upsert[A](elem:A,pos:Int,ls:List[A]):List[A] = ls.take(pos) ::: List(elem) ::: ls.drop(pos)

  println(upsert("new",1,myList))

  def upsert2[A](elem:A,pos:Int,ls:List[A]):List[A] = ls.splitAt(pos) match {
    case (pre,post) => pre ::: List(elem) ::: post
  }
  println(upsert2("new",1,myList))
}
