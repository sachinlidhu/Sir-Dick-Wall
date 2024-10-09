package akkaStreams_Part4_techniques

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}

import java.util.Date
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object AdvancedBackpressure extends App{

  implicit val system = ActorSystem("AdvancedBackpressure")
  implicit val materializer = ActorMaterializer

  //control backpressure
  val controlledFlow = Flow[Int].map(_*2).buffer(10, OverflowStrategy.dropHead)

  case class PagerEvent(description: String, date: Date, nInstances: Int = 1)
  case class Notification(email:String, pagerEvent: PagerEvent)

  val events = List(
    PagerEvent("Service discovery failed", new Date),
    PagerEvent("Illegal elements in the data pipeline", new Date),
    PagerEvent("Number of http 500 spiked", new Date),
    PagerEvent("A Service stopped responding", new Date)
  )
  val eventSource = Source(events)

  val oncallEngineer = "sachin@gmail.com" //a fast service for fetching oncall emails

  def sendEmail(notification: Notification) =
    println(s"dear ${notification.email}, you have an event: ${notification.pagerEvent}")

  val notificationSink = Flow[PagerEvent].map(event => Notification(oncallEngineer, event))
    .to(Sink.foreach[Notification](sendEmail))


  //eventSource.to(notificationSink).run()

  def sendEmailSlow(notification: Notification) = {
    Thread.sleep(1000)
    println(s"Dear ${notification.email}, you have an event : ${notification.pagerEvent}")
  }

  val aggregateNtificationFlow = Flow[PagerEvent]
    .conflate((event1, event2) => {
    val nInstances = event1.nInstances + event2.nInstances
    PagerEvent(s"u have $nInstances events that require your attention", new Date, nInstances)
  })
    .map(resultingEvent => Notification(oncallEngineer, resultingEvent))

  eventSource.via(aggregateNtificationFlow).async.to(Sink.foreach[Notification](sendEmailSlow)).run()
  //above one alternative to backpressure

  /*
  another technique which is good for fast producer and slow consumer
   */
  val slowCounter = Source(LazyList.from(1)).throttle(1, 1 second)
  val hungrySink = Sink.foreach[Int](println)

  val extrapolator = Flow[Int].extrapolate(element => Iterator.from(element))

  slowCounter.via(extrapolator).to(hungrySink).run()

  val expander = Flow[Int].expand(element => Iterator.from(element))
  //expand works similarv to extrapolator but with a twist
  // extrapolator creates iterator only when there is a demand
  // but expand creates it all the time
}
