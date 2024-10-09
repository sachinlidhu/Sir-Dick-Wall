package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ActorCapabilities.BankAccount.{Deposit, Withdraw}
import part2actors.ActorCapabilities.Counter.{Decrement, Increment, Print}
import part2actors.ActorCapabilities.Person.LiveTheLife

object ActorCapabilities extends App{

  class SimpleActor extends Actor {
    //instead of context.self, we can use self only also
    override def receive: Receive = {
      case "Hiiiii!!!" => context.sender() ! "Hello there!" //replying to message
      case message: String => println(s"[$self] I have received $message")
      case number: Int => println(s"[simple actor] I have received a number: $number")
      case SpecialMessage(content) => println(s"[simple actor] I have received special: $content")
      case SendMessageToYouself(content) => self ! content
      case SayHiTo(ref) =>ref ! "Hiiiii!!!"
      case WirelessPhoneMessage(content, ref) => ref forward content //instead of ref!content ....forward keep the original sender of the WPM
    }
  }
  val system = ActorSystem("actorCapabilities")
  val simpleActor = system.actorOf(Props[SimpleActor],"simpleactor")

  simpleActor ! "helllo actor"
  simpleActor ! 42

  //u can send any message under 2 conditions
  //1..message must be immutable. 2... message must be serializable---
  // thats why we often use case classes and case objects to send messages
  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("SOME S[ECIAL CONTENT")

  //actors have information about their context and about themselves
  //context.self has information to that actor Ref
  //context.self == this in OOP
  //it can be useful to send message to urseelf

  case class SendMessageToYouself(content: String)
  simpleActor ! SendMessageToYouself("I am an Actor, I am proud of it")

  //3 - how actors can reply to messages
  val alice = system.actorOf(Props[SimpleActor],"alice")
  val bob = system.actorOf(Props[SimpleActor],"bob")

  case class SayHiTo(ref: ActorRef)
  alice ! SayHiTo(bob)

  //4 - deadletters
  alice ! "Hiiiii!!!" // here slice is sending message its its own reference

  //5 - how actors forward message to one another
  //forwarding = sending a message with original sender

  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  alice ! WirelessPhoneMessage("Hi",bob)

  /**
   * Exercises
   * 1. a counter actor
   *  -increment,decrement,print
   *
   *  2. create a bank account as a actor
   *    - deposit amount
   *    -withdraw amount
   *    -statement
   *
   *    reply with suucess/failure
   */

  object Counter{
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor{
    import Counter._
    var count = 0

    override def receive: Receive = {
      case Increment => count += 1
      case Decrement => count -= 1
      case Print => println(s"[counter] my current count is $count")
    }
  }
  val counter = system.actorOf(Props[Counter],"mycounter")
  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 3).foreach(_ => counter ! Decrement) //how can i make sure this decrement is working only after imcrement..its asyncgronous so it can work first also.
  counter ! Print

  //bank account
  object BankAccount{
    case class Deposit(amount:Int)
    case class Withdraw(amount: Int)
    case object Statment

    case class TransactionSuccess(message: String)
    case class TransactionFailure(reason: String)
  }
  class BankAccount extends Actor{
    import BankAccount._
    var funds = 0

    override def receive: Receive = {
      case Deposit(amount) =>
        if (amount < 0) sender() ! TransactionFailure("invalid deposit amount")
        else{
          funds += amount
          sender() ! TransactionSuccess(s"successfully deposited $amount")
        }
      case Withdraw(amount) =>
        if (amount < 0) sender() ! TransactionFailure(s"invalid withdraw amount")
        else if (amount > funds) sender() ! TransactionFailure(s"insufficient funds")
        else{
          funds -= amount
          sender() ! TransactionSuccess(s"succesfully withdrew $amount")
        }
      case Statment => sender() ! s"Your balance is $funds"
    }
  }
  object Person{
    case class LiveTheLife(account: ActorRef)
  }
  class Person extends Actor{
    import Person._
    import BankAccount._
    override def receive: Receive = {
      case LiveTheLife(account) =>
        account ! Deposit(10000)
        account ! Withdraw(90000)
        account ! Withdraw(500)
        account ! Statment
      case message => println(message.toString)
    }
  }
  val account = system.actorOf(Props[BankAccount])
  val person = system.actorOf(Props[Person])

  person ! LiveTheLife(account)
}
