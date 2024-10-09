val a = 10
val b = 20

if (a > b) a else b

if (a > b) println(s"max is $a") else println(s"max is $b")

var doIt: Boolean = true
val result = while (doIt) {
  println("Hello")
  doIt = false
}

var x = 5
val y = x = 10 //val and var return unit also

println(x)
println(y)

val div = 0

val n = try {
  x / div
} catch {
  case ex: ArithmeticException => 0
}

def add(a: Int, b: Int) = {
  val result = a + b //note val return unit
  //result
}

add(5, 6)

//try, if , for() are expressions
// but while, do while are statement, and in FP we prefer expression
// as statements have side effects (returns unit)
