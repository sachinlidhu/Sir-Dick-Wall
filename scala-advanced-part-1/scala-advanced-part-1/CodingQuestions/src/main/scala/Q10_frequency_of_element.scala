/**
 * P10 (*) Run-length encoding of a list.
Use the result of problem P09 to implement the so-called run-length encoding data compression method.  Consecutive duplicates of elements are encoded as tuples (N, E) where
N is the number of duplicates of the element

Example:

scala> encode(List('a, 'a, 'a, 'a, 'b, 'c, 'c, 'a, 'a, 'd, 'e, 'e, 'e, 'e))
res0: List[(Int, Symbol)] = List((4,'a), (1,'b), (2,'c), (2,'a), (1,'d), (4,'e))
 */
object Q10_frequency_of_element extends App{

  val myList = List('a', 'a', 'a', 'a', 'b', 'c', 'c', 'a', 'a', 'd', 'e', 'e', 'e', 'e')
  def club[A](ls:List[A]):List[List[A]] = {
    if(ls.isEmpty) Nil
    else{
      val (packed, next) = ls.span(_==ls.head)
      if(next==Nil) List(packed) else packed::club(next)
    }
  }
  val a = club(myList).map(x=>(x.length,x.head))
  println(a)

  //     Example:
  //     scala> encodeModified(List('a, 'a, 'a, 'a, 'b, 'c, 'c, 'a, 'a, 'd, 'e, 'e, 'e, 'e))
  //     res0: List[Any] = List((4,'a), 'b, (2,'c), (2,'a), 'd, (4,'e))

  val modified = a.map(x => if(x._1 == 1) x._2 else x)
  println(modified)
}
