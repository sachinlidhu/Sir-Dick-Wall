import scala.concurrent.Future
import scala.util.{Failure, Success}

//multi threading recap
object Multithreading extends App {

  //lets first start with creating threads on jvm........thread class which receives a runnable object in where method called run does something

  val aThread = new Thread (new Runnable {
    override def run(): Unit = println("i am runnniig in parrrel")
  })

  //for that scala provides some syntactic sugar like

  val aThread1 = new Thread(() => println("i m running in parallel"))
  aThread.start //to start the thread

  //threads are good as we have multicore processors and so many things can be work in parrelle
  //problem is that they r unpredictable
   val threadHello = new Thread(() => (1 to 1000).foreach(_ => println("hello")))
   val threadBye = new Thread(() => (1 to 1000).foreach(_ => println("bye")))

  // if we start thread hello and bye...we will see mixes of results some bye and some hello...why?
  //different runs produce different results
  threadHello.start()
  threadBye.start()

  //bank account example
  class BankAccount(private var amount: Int) {
    override def toString: String = "" + amount

    def withdraw(money: Int) = this.amount -= money //u can imagine this method is not thread safe
    /*
    lets say we have bank account with 10K ruppes
    and we initiated two threads at the same time. one with withdraw 1K other with withdraw 2k
    let say withdraw 2k thread completes first - 8k
    then withdraw 1k - 9k

    we get answer 9k ...it takes result of most recent thread...so it is a problem

    to solve this problem we add synchronised keyword
     */
    //  but synchronisation may lead to deadlocks, livelocks,

    def safeWithdraw(money: Int) = this.synchronized {
      this.amount -= money
    }

    //or add @volatile to parameter which does the same thing but it only work for primitive type such as Int
  }
  //this problem of synchronised threads becomes increasingly difficult for bigger and complex ur application becomes.
  //for large application its a huge pain in the neck

  /*
  inter-thread communication which is done done via wait and notify mechanism...
  if we have multiple backgrounds tasks how can we check which task is doing what and on whose orders
  ----------its very complex and hard for developers
   */

  /*
  another way to deal with threads
  scala future also works on threads but as we know threads works independently
   and can return individual results at different time in future, and can handle success and failures
   */

  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    //long computation on different threads
    42
  }
  future.onComplete {
    case Success(42) => println("I found the message of life")
    case Failure(_) => println("something happened with the meaning of life!")
  }

  /*
  as we said traditional thread model may leads to dead locks, live locka etc
  so we need a data structure which is fully encapsulated with no locks.
  which can safely receive messages and indentify the sender.
  which is easily identifiable and guard against the errors.

   */
  /*
  tracing and dealing with errorrs in a multithreaded env is pain in the neck.
  lets assume we need to find sum of 1M numbers in between 10 threads.
  we can create 10 threads and a shared resources for that, but lets say we are super smart and use futures for that
   */

  val futures = (1 to 10)
    .map(i => 100000*i until 10000*(i+1)) // 0 - 99999, 100000 -199999, 2000000-299999 etc
    .map(range => Future {
      if (range.contains(546735)) throw new RuntimeException("invalid number")
      range.sum
    })
  val sumFuture = Future.reduceLeft(futures)(_ + _) //Future with the sum of all the numbers
  sumFuture.onComplete(println)
  //here sometimes it run with failure and show result which u have caught as runtime exception which is a nightmare

  //this is also drawback of thread model.

}