package arithamatic

/**P31 (**) Determine whether a given integer number is prime.
  *scala> 7.isPrime
*res0: Boolean = true*/

object PrimeNumber extends App {

  implicit class PrimeNumber(start: Int) {
    def isPrime: Boolean = primes.takeWhile(_ <= Math.sqrt(start)).forall(_ % 2 != 0)
  }
  val primes = LazyList.cons(2,LazyList.from(3,2).filter{_.isPrime})
}