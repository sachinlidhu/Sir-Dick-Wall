/**
 * P12 (**) Decode a run-length encoded list.
Given a run-length code list generated as specified in problem P10, construct its uncompressed version.

Example:

scala> decode(List((4, 'a), (1, 'b), (2, 'c), (2, 'a), (1, 'd), (4, 'e)))
res0: List[Symbol] = List('a, 'a, 'a, 'a, 'b, 'c, 'c, 'a, 'a, 'd, 'e, 'e, 'e, 'e)
 */
object Q11_DecodeRunLenght extends App{

  val myList = List((4, 'a'), (1, 'b'), (2, 'c'), (2, 'a'), (1, 'd'), (4, 'e'))

  val a = myList.flatMap(x => List.fill(x._1)(x._2))
  println(a)

  //------------------------------------------------------
  /**
   * val vs var
   *
   */

   val abc:Int = 5//mutable--change
  val aString: String = "hello"
  val anotherString = "goodbye nihdihd  jdkwdj"

  val aBoolean: Boolean = false
  val aChar: Char = 'a'
  val anInt: Int = 'y'
  val aShort: Short = 4613
  val aLong: Long = 5273985273895237L
  val aFloat: Float = 2.0f
  val aDouble: Double = 3.14

  var i = 0
  while (i < 10) {
    println(i)
    i += 1
  }

  //unit is side effect and we have to tackle it, avoid using it

  //expression vs statements --staetments--println, var, while etc
  //scala preferes expression

  val n = println("Tarun") //statement
  val m = "Tarun" //expression
 
}
