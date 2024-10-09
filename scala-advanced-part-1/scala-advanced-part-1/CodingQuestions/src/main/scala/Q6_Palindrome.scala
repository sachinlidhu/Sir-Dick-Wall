/**
 * P06 (*) Find out whether a list is a palindrome.
Example:

scala> isPalindrome(List(1, 2, 3, 2, 1))
res0: Boolean = true
 */

object Q6_Palindrome extends App{

  val myList = List(1,2,3,2,1)

  println(myList == myList.reverse)
}
