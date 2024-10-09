/**
 * P22 (*) Create a list containing all integers within a given range.
Example:

scala> range(4, 9)
res0: List[Int] = List(4, 5, 6, 7, 8, 9)
 */
object Q20_CreateListWithinGivenRange extends App{

  def sol(start:Int, end:Int):List[Int] = (start,end) match {
    case (start,end) if start == end  => List(end):::Nil
    case (start,end) =>List(start) ::: sol(start + 1, end)
  }
  println(sol(4,9))
}
