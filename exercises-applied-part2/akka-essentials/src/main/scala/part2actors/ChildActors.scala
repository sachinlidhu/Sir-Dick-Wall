package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChildActors.CreditCard.{AttachToAccount, CheckStatus}
import part2actors.ChildActors.Parent.CreateChild

object ChildActors extends App{
  //actors can create other actors
  object Parent{
    case class CreateChild(name: String)
    case class TellChild(message: String)
  }
  class Parent extends Actor{
    import Parent._

    var child: ActorRef = null
    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} creating child")
        //freate a new actor right here
        val childRef = context.actorOf(Props[Child],name)
        context.become(withChild(childRef))//child = childRef
    }
    def withChild(childRef: ActorRef): Receive ={
      case TellChild(message) =>
        if (childRef != null) childRef forward (message)
    }
  }

  class Child extends Actor{
    override def receive: Receive = {
      case message => println(s"${self.path} I got: $message")
    }
  }
  import Parent._
  val system = ActorSystem("parentChildDemo")
  val parent = system.actorOf(Props[Parent],"parent")
  parent ! CreateChild("child")
  parent ! TellChild("hey kid")

  //Actor hierarchies
  //parent -> child -> grandchild
  //       -> child2 ->

  /*
  Guardian actors (top-level)
  - /system = system guardian
  - /user = user-level guardian
  - / = the root guardian
   */

  /**
   * Actor selection
   */
  val childSelection = system.actorSelection("/user/parent/child")
  //val childSelection = system.actorSelection("/user/parent/child2")
  childSelection ! " I found you"

  /**
   * danger-------naver pass mutable actor state or `this` reference to child actors
   */
  object NaiveBankAccount{
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
    case object InitializeAccount
  }
  class NaiveBankAccount extends Actor{
    import NaiveBankAccount._
    import CreditCard._
    var amount = 0
    override def receive: Receive = {
      case InitializeAccount =>
        val creditCardRef = context.actorOf(Props[CreditCard])
        creditCardRef ! AttachToAccount(this)// !!
      case Deposit(funds) => deposit(funds)
      case Withdraw(funds) => withdraw(funds)

    }
    def deposit(funds: Int) = {
      println(s"${self.path} depositing $funds on top of $amount")
      amount += funds
    }
    def withdraw(funds: Int) = {
      println(s"${self.path} withdrawing $funds from $amount")
      amount -= funds
    }
  }

  object CreditCard{
    case class AttachToAccount(bankAccount: NaiveBankAccount) //!!
    case object CheckStatus
  }
  class CreditCard extends Actor{
    override def receive: Receive = {
      case AttachToAccount(account) => context.become(attachedTo(account))
    }
    def attachedTo(account: ChildActors.NaiveBankAccount): Receive ={
      case CheckStatus =>
        println(s"${self.path} your message has been processed")
        account.withdraw(1)
    }
  }

  import NaiveBankAccount._
  import  CreditCard._

  val bankAccountRef = system.actorOf(Props[NaiveBankAccount])
  bankAccountRef ! InitializeAccount
  bankAccountRef ! Deposit(1000)

  Thread.sleep(500)
  val ccSelection = system.actorSelection("/user/account/card")
  ccSelection ! CheckStatus
}
