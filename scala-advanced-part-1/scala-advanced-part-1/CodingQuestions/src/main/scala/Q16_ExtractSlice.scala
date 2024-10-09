/**
 * P18 (**) Extract a slice from a list.
Given two indices,I and K, the slice is the list containing the elements from and including the
Ith element up to but not including the
Kth element of the original list.  Start counting the elements with 0.

Example:

scala> slice(3, 7, List('a, 'b, 'c, 'd, 'e, 'f, 'g, 'h, 'i, 'j, 'k))
res0: List[Symbol] = List('d, 'e, 'f, 'g)
 */
object Q16_ExtractSlice extends App{

  val myList = List('a','b','c','d','e','f','g','h','i','j','k')

  def extract[A](start:Int,end:Int, ls:List[A]):List[A] = (start,end, ls) match{
    case (0,0,ls) => Nil
    case (0,end, h::tail) => h :: extract(0,end-1,tail)
    case (start,end, h::tail) => extract(start -1,end-1, tail)
  }
  println(extract(3,7,myList))

  def extractFunctional[A](start:Int, end:Int, ls:List[A]):List[A] = ls.drop(start).take(end-start)
  println(extractFunctional(3,7,myList))
}
