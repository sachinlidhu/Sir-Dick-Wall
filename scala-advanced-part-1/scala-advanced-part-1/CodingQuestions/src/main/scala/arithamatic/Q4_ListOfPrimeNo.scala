package arithamatic

import scala.collection.IterableOnce.iterableOnceExtensionMethods

/**
 * P39 (*) A list of prime numbers.
*Given a range of integers by its lower and upper limit, construct a list of all prime numbers in that range.
 *
 *scala> listPrimesinRange(7 to 31)
*res0: List[Int] = List(7, 11, 13, 17, 19, 23, 29, 31)
 */
object Q4_ListOfPrimeNo extends App{

  val primes = LazyList.cons(2,LazyList.from(3,2
  )
    .filter(x => (2 to Math.sqrt(x).toInt)
      .forall(y => (x%y)!=0)))

  def listOfPrime(r: Range):List[Int] = primes.dropWhile(_< r.start).takeWhile(_<r.end).toList

  println(listOfPrime(7 to 31))
}
