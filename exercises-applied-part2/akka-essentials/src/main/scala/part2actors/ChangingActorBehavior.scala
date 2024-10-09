package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ActorCapabilities.{Counter, system}
import part2actors.ChangingActorBehavior.FuzzyKid.{HAPPY, SAD}
import part2actors.ChangingActorBehavior.Mom.MomStart

object ChangingActorBehavior extends App{

  class FuzzyKid extends Actor{
    import FuzzyKid._
    import Mom._
    var state = HAPPY
    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(_) =>
        if (state == HAPPY) sender() ! KidAccept
        else  sender() ! KidReject
    }
  }
  object FuzzyKid{
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD = "sad"
  }
  class Mom extends Actor{
    import Mom._
    import FuzzyKid._
    override def receive: Receive = {
      case MomStart(kidRef) =>
        kidRef ! Food(VEGETABLE)
        kidRef ! Ask(s"Do u want to play?")
      case KidAccept => println(s"Yay, my kid id happy")
      case KidReject => println(s"My kid is sad but he is healthy")
    }
  }
  object Mom{
    case class MomStart(kidRef: ActorRef)
    case class Food(food: String)
    case class Ask(message: String)
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }
  val system = ActorSystem("changingBehavior")
  val fuzzyKid = system.actorOf(Props[FuzzyKid])
  val mom = system.actorOf(Props[Mom])
  val statelessFussyKid = system.actorOf(Props[StatelessFussyKid])

 // mom ! MomStart(fuzzyKid)
  mom ! MomStart(statelessFussyKid)
  //try to do the same without `var`.......try context.become

  class StatelessFussyKid extends Actor{
    import FuzzyKid._
    import Mom._
    override def receive: Receive = happyReceive
    def happyReceive: Receive = {
      case Food(VEGETABLE) =>  context.become(sadReceive)//CHANGE MY RECEIVE HANDLER TO SADRECEIVE
      case Food(CHOCOLATE) =>
      case Ask(_) => sender() ! KidAccept
    }
    def sadReceive: Receive = {
      case Food(VEGETABLE) =>
      case Food(CHOCOLATE) => context.become(happyReceive)//change my receive handler to happy Receive
      case Ask(_) => sender() ! KidReject
    }
    //context.become(sadReceive,true)--true means fully fully it with sadReceive
    //context.become(sadReceive,false)--instead of fully replacing with sedReceive, it will put it on stack of old meesage handler

  }

  /**
   * Exercises
   * 1 - recreate the counter actor with context.become and no mutable state.
   * 2 - SIMPLIFIED voting system
   */
  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {
    import Counter._

    override def receive: Receive = countReceive(0)

    def countReceive(currentCount: Int): Receive = {
      case Increment =>
        println(s"[$currentCount] incrementing")
        context.become(countReceive(currentCount+1))
      case Decrement =>
        println(s"[$currentCount] decrementing")
        context.become((countReceive(currentCount - 1)))
      case Print => println(s"[counter] my current count is $currentCount")
    }
  }
  import Counter._

  val counter = system.actorOf(Props[Counter], "mycounter")
  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 3).foreach(_ => counter ! Decrement) //how can i make sure this decrement is working only after imcrement..its asyncgronous so it can work first also.
  counter ! Print

  /**
   * Exercise 2 - a simplified voting system
   */

  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])
  class Citizen extends Actor{
    //var candidate: Option[String] = None
    override def receive: Receive = {
      case Vote(c) => context.become(voted(Some(c)))//candidate = Some(c)
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }
    def voted(candidate: Option[String]): Receive ={
      case VoteStatusRequest => sender() ! VoteStatusReply(candidate)
    }
  }

  case class AggregateVotes(citizens: Set[ActorRef])
  class VoteAggregator extends Actor{
    var stillWaiting: Set[ActorRef] =Set()
    var currentStats: Map[String, Int] = Map()
    override def receive: Receive = {
      case AggregateVotes(citizens) =>
        stillWaiting = citizens
        citizens.foreach(citizenRef => citizenRef ! VoteStatusRequest)
      case VoteStatusReply(None) =>
        //citizen hasnt voted yet
        sender() ! VoteStatusRequest
      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidate = currentStats.getOrElse(candidate, 0)
        currentStats = currentStats + (candidate -> (currentVotesOfCandidate + 1))
        if (newStillWaiting.isEmpty){
          println(s"[aggregator] poll stats: $currentStats")
        } else {
          stillWaiting = newStillWaiting
        }
    }
    def awaitingCommand: Receive ={
      case AggregateVotes(citizens) =>
        citizens.foreach(citizenRef => citizenRef ! VoteStatusRequest)
        context.become(awaitingStatuses(citizens, Map()))
    }
    def awaitingStatuses(stillWaiting: Set[ActorRef], currentStats: Map[String, Int]): Receive ={
      case VoteStatusReply(None) =>
        //citizen hasnt voted yet
        sender() ! VoteStatusRequest
      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidate = currentStats.getOrElse(candidate, 0)
        val newStats = currentStats + (candidate -> (currentVotesOfCandidate + 1))
        if (newStillWaiting.isEmpty) {
          println(s"[aggregator] poll stats: $currentStats")
        } else {
          context.become(awaitingStatuses(newStillWaiting,newStats))
        }
    }
  }
  val alice = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val charlie = system.actorOf(Props[Citizen])
  val daniel = system.actorOf(Props[Citizen])

  alice ! Vote("Martin")
  bob ! Vote("Jones")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")

  val voteAggregator = system.actorOf(Props[VoteAggregator])
  voteAggregator ! AggregateVotes(Set(alice,bob,charlie,daniel))
  /*
  print the status of the vote
  martin ->1
  jonas ->1
  roland -> 2
   */
}
