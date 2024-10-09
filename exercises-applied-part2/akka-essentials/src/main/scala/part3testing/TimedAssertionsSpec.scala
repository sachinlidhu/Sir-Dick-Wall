package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random

class TimedAssertionsSpec extends TestKit(
  ActorSystem("TimedAssertionsSpec", ConfigFactory.load().getConfig("specialTimedAssertionsConfig")))
with ImplicitSender
with AnyWordSpecLike
with BeforeAndAfterAll {

    override def afterAll(): Unit = {
      TestKit.shutdownActorSystem(system)
    }
  import TimedAssertionsSpec._

  "A worker actor" should{
    val workerActor = system.actorOf(Props[WorkerActor])
    "reply with meaning of life in timely manner" in {
      within(500 millis, 1 second){
        workerActor ! "work"
        expectMsg(WorkResult(42))
      }
    }
    "this should fail if u choose 300 milisecond atmost as its too short time for any avtor to respond" in {
      within(300 millis) {
        workerActor ! "work"
        expectMsg(WorkResult(42))
      }
    }
    "reply with valid work at a reasonable cadence" in{
      within(1 second){
        workerActor ! "workSequence"

        val results = receiveWhile[Int](max = 2 second, idle=500 millis, messages = 10){
          case WorkResult(result) => result
        }
        assert(results.sum > 5)
      }
    }
    "reply to a test probe in a timely manner" in {
      within(1 second) {
        val probe = TestProbe()
        probe.send(workerActor, "work")
        probe.expectMsg(WorkResult(42))
      }
    }
  }
}
object TimedAssertionsSpec{

  case class WorkResult(result: Int)

  class WorkerActor extends Actor {
    override def receive: Receive = {
      case "work" =>
        //long computation
        Thread.sleep(500)//if i comment out this it will fail because thread need atleast 500 milisecond
        sender ! WorkResult(42)
      case "workSequence" =>
        val r = new Random()
        for(i <- 1 to 10){
          Thread.sleep(r.nextInt(50))
          sender() ! WorkResult(1)
        }
    }
  }
}
