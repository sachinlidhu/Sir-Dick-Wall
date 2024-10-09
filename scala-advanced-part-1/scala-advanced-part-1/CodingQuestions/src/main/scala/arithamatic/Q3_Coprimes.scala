package arithamatic

/**
 * P33 (*) Determine whether two positive integer numbers are coprime.
Two numbers are coprime if their greatest common divisor equals 1.

scala> 35.isCoprimeTo(64)
res0: Boolean = true
 */
object Q3_Coprimes extends App{

  def coprime(a: Int, b: Int):Boolean = {
    def gcd(a: Int, b: Int): Int = if (a == 0) b else gcd(b % a, a)
    if(gcd(a,b) == 1) true else false
  }
  println(coprime(35,64))
}
