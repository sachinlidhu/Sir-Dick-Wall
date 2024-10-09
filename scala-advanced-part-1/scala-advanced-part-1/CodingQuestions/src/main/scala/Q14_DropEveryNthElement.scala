/**
 * P16 (**) Drop every
N
Nth element from a list.
Example:

scala> drop(3, List('a, 'b, 'c, 'd, 'e, 'f, 'g, 'h, 'i, 'j, 'k))
res0: List[Symbol] = List('a, 'b, 'd, 'e, 'g, 'h, 'j, 'k)
 */
object Q14_DropEveryNthElement extends App{

  val myList = List('a','b','c','d','e','f','g','h','i','j','k')

  def a[A](n:Int, givenList:List[A]):List[A] = {
    givenList.zipWithIndex.filter(x => ((x._2) + 1) % n != 0).map(_._1)
  }

  def b[A](n:Int, givenList:List[A]):List[A] = (n,givenList) match{
    case (_,Nil) => Nil
    case (1,_::tail) => b(n,tail)
    case (_,h::tail) =>h::b(n-1,tail)
  }
  //above function also needs another parameter to reset value of n

  def dropR[A](n:Int, c: Int, curList: List[A]): List[A] = (n,c, curList) match {
    case (_,_, Nil) => Nil
    case (n,1, _ :: tail) => dropR(n,n, tail)
    case (_,_, h :: tail) => h :: dropR(n,c - 1, tail)
  }

  println(dropR(3,3, myList))

  println(a(3,myList))
  println(b(3,myList))

  //using tailrecurssion
  def dropRtailRecursive[A](n:Int,c:Int,ls:List[A],res:List[A]):List[A] = (c,ls) match{
    case (_,Nil) => res.reverse
    case (1,_::tail) => dropRtailRecursive(n,n,tail,res)
    case (_,h::tail) => dropRtailRecursive(n,c-1,tail,h::res)
  }
  println(dropRtailRecursive(3,3,myList,Nil))

}
