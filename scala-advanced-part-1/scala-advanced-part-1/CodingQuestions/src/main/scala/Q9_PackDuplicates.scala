/**
 * P09 (**) Pack consecutive duplicates of list elements into sublists.
If a list contains repeated elements they should be placed in separate sublists.

Example:

scala> pack(List('a, 'a, 'a, 'a, 'b, 'c, 'c, 'a, 'a, 'd, 'e, 'e, 'e, 'e))
res0: List[List[Symbol]] = List(List('a, 'a, 'a, 'a), List('b), List('c, 'c), List('a, 'a), List('d), List('e, 'e, 'e, 'e))
 */
object Q9_PackDuplicates extends App{

  val myList = List('a', 'a', 'a', 'a', 'b', 'c', 'c', 'a', 'a', 'd', 'e', 'e', 'e', 'e')

  def recursive[A](ls:List[A]):List[List[A]] = {
    if(ls.isEmpty) Nil
    else{
      val (packed, next) = ls.span(_ == ls.head)
      if (next == Nil) List(packed)
      else
      packed :: recursive(next)
    }
  }
  println(recursive(myList))
/*  myList.foldLeft(List()){(x,y) =>
    y.span
  }*/

}
