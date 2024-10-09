package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike
import part3testing.BasicSpec.{Blackhole, LabTestActoe, SimpleActor}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll{
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
  "A simple actor" should{
    "send back thr same message" in{
      val echoActor = system.actorOf(Props[SimpleActor])
      val message = "hello, test"
      echoActor ! message

      expectMsg(message)
      println(message)
    }
    "A blackhole actor" should{
      "send back some message" in{
        val blackhole = system.actorOf(Props[Blackhole])
        val message = " hello......"
        blackhole ! message

        //expectMsg(message)
        expectNoMessage(1 second) // as we know that class dont show any message
      }
    }
    //message assertions
    "A lab test actor" should{
      val labTestActor = system.actorOf(Props[LabTestActoe])
      "turn a string to uppercase" in{
        labTestActor ! "I love Akka"
        //expectMsg("I LOVE AKKA") //there arae nicer ways to do this
        val reply = expectMsgType[String]

        assert(reply == "I LOVE AKKA")
      }
      "reply to greeting" in {
        labTestActor ! "greeting"
        expectMsgAnyOf("hi","hllo")
      }
      "reply with favorite tech" in{
        labTestActor ! "favoriteTech"
        expectMsgAllOf("scala","Akka")
      }
      "reply with cool tech in a different way" in {
        labTestActor ! "favoriteTech"
        val messages = receiveN(2) //it means if i receive less than 2 messages in a time frame then its gonna fail

      }
      "reply with cool tech in a fancy way" in{
        labTestActor ! "favoriteTech"

        expectMsgPF(){
          case "scala" =>
          case "Akka" =>
        }
      }
    }
  }
}
object BasicSpec{
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message => sender() ! message
    }
  }
  class Blackhole extends Actor{
    override def receive: Receive = Actor.emptyBehavior
  }
  class LabTestActoe extends Actor {
    val random = new Random()
    override def receive: Receive = {
      case "greeting" =>
        if (random.nextBoolean()) sender() ! "hi" else sender() ! "hllo"
      case "favoriteTech" =>
        sender() ! "scala"
        sender() ! "Akka"
      case message:String => sender() ! message.toUpperCase
    }
  }
}
