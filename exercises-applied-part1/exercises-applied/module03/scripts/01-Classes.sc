
class Demo

class DemoWithFieldsAndMethods {
  val x = 10
  val y = x * 2

  def timesY(a: Int): Int = a * y
}

val demoWithFieldsAndMethods = new DemoWithFieldsAndMethods

demoWithFieldsAndMethods.x
demoWithFieldsAndMethods.timesY(4)

class DemoWithParams(name: String) {
  println(s"Constructing for $name")

  def sayHi(times: Int): Unit = {
    var time = 0

    while (time < times) {
      println(s"Hi, $name")
      time += 1
    }
  }
}

val demoWithParams = new DemoWithParams("Val")

demoWithParams.sayHi(5)

// can't access the name from outside:
// demoWithParams.name because this paramater is private
//if we want to use it we have to make it available,
// for that use val inside parametr or assign it to val seperately
//class demo(val name)