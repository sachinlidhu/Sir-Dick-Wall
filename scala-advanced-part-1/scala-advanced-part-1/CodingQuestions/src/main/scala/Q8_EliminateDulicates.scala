import Q8_EliminateDulicates.a

import scala.::

/**
 * P08 (**) Eliminate consecutive duplicates of list elements.
If a list contains repeated elements they should be replaced with a single copy of the element.  The order of the elements should not be changed.

Example:

scala> compress(List('a, 'a, 'a, 'a, 'b, 'c, 'c, 'a, 'a, 'd, 'e, 'e, 'e, 'e))
res0: List[Symbol] = List('a, 'b, 'c, 'a, 'd, 'e)
 */
object Q8_EliminateDulicates extends App{

  val myList = List('a', 'a', 'a', 'a', 'b', 'c', 'c', 'a', 'a', 'd', 'e', 'e', 'e', 'e')

  val a = myList.foldRight(List[Char]()){
    (a,b) =>
      if(b.isEmpty || b.head!=a) {
       // println("wwwwwwwwwwww"+ a)
       // println("bbbbbbbbbbbb"+ b)
        a :: b
      }
      else b
  }

  println(a)


  def recurcive[A](ls:List[A]): List[A] = ls match {
    case Nil => Nil
    case h::tail => h::recurcive(tail.dropWhile(_==h))
  }

  println(recurcive(myList))

  def tailRecursive[A](res:List[A],ls:List[A]):List[A] = ls match {
    case Nil => res.reverse
    case h::tail => tailRecursive(h::res, tail.dropWhile(_==h))
  }

  println(tailRecursive(Nil,myList))

}
