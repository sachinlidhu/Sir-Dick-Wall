/**P14 (*) Duplicate the elements of a list.
  Example:

  scala> duplicate(List('a, 'b, 'c, 'c, 'd))
res0: List[Symbol] = List('a, 'a, 'b, 'b, 'c, 'c, 'c, 'c, 'd, 'd)*/

object Q12_DuplicateTheElemOfList extends App{
  val myList = List('a','b','c','c','d')

  val a = myList.flatMap(x => List(x,x))
  val b = myList.map(x =>List(x,x))
  println("flatmap ----->"+a)
  println("map ----->"+b)

}
