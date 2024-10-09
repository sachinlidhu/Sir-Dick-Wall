package akkaStreams_Part4_techniques

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.scalatest.concurrent.PatienceConfiguration.Timeout

import java.util
import java.util.Date
import scala.concurrent.Future

object IntegratingWithExternalServices extends App{

  implicit val system = ActorSystem("IntegratingWithExternalServices")
  implicit val materializer = ActorMaterializer
  //import system.dispatcher //not recommended in practice for mapAsync
  implicit val dispatcher = system.dispatchers.lookup("dedicated-dispatcher") //define this in application.conf

  def genericExternalService[A,B](element: A): Future[B] = ???

  //example: simplified pagerDuty
  case class PagerEvent(application: String, description: String, date: Date)
  //these kind of events are pushed to our service contineously via some kind of Api
  // that we expose to other people
  //and in the end get some source that we define below

  val eventSource = Source(List(
    PagerEvent("AkkaInfra", "Infrastructure broke", new Date),
    PagerEvent("FastDataPipeline", "Illegal elements in the data pipeline", new Date),
    PagerEvent("AkkaInfra", "A service stopped responding", new Date),
    PagerEvent("SuperFrontend", "A button doesnot work", new Date)
  ))

  object PagerService {
    private val engineers = List("Daniel", "John", "lady gaga")
    private val emails = Map(
      "Daniel" -> "danel@rockthejvm.com",
      "John" -> "john@rockthejvm.com",
      "Lady gaga" -> "ladygaga@rtjvm.com"
    )

    def processEvent(pagerEvent: PagerEvent) = Future {
      val engineerIndex = (pagerEvent.date.toInstant.getEpochSecond / (24 * 3600)) % engineers.length
      val engineer = engineers(engineerIndex.toInt)
      val engineerEmail = emails(engineer)

      //page the engineer
      println(s"Sending engineer $engineerEmail a high priorty notification: $pagerEvent")
      Thread.sleep(1000)

      //return the email that was paged
      engineerEmail
    }
  }

  val infraEvents = eventSource.filter(_.application == "AkkaInfra")
  val pagedEngineerEmails = infraEvents.mapAsync(parallelism = 4)(event => PagerService.processEvent(event))
  //mapAsync guarentees relative order of elements regardless of which future is faster.
  //it takes extra parameter for parallelism which means it will launch that many future threads
  // if any one fails entire flow will stop
  val pagedEmailsSink = Sink.foreach[String](email => println(s"Successfully sent notification to $email"))

  //pagedEngineerEmails.to(pagedEmailsSink).run()

  class PagerActor extends Actor with ActorLogging {


    private val engineers = List("Daniel", "John", "lady gaga")
    private val emails = Map(
      "Daniel" -> "danel@rockthejvm.com",
      "John" -> "john@rockthejvm.com",
      "Lady gaga" -> "ladygaga@rtjvm.com"
    )

    private def processEvent(pagerEvent: PagerEvent) = Future {
      val engineerIndex = (pagerEvent.date.toInstant.getEpochSecond / (24 * 3600)) % engineers.length
      val engineer = engineers(engineerIndex.toInt)
      val engineerEmail = emails(engineer)

      //page the engineer
      log.info(s"Sending engineer $engineerEmail a high priorty notification: $pagerEvent")
      Thread.sleep(1000)

      //return the email that was paged
      engineerEmail
    }

    override def receive: Receive = {
      case pagerEvent: PagerEvent =>
        sender() ! processEvent(pagerEvent)
    }
  }

  import scala.concurrent.duration.DurationInt
  import scala.language.postfixOps
  import akka.pattern.ask
  implicit val timeout = Timeout(2 seconds)
  val pagerActor = system.actorOf(Props[PagerActor],"pageActor")
  //val alternativePagedEngineerEmails =infraEvents.mapAsync(parallelism = 4)(event => (pagerActor ? event).mapTo[String])
  //alternativePagedEngineerEmails.to(pagedEmailsSink).run()

  //dont comfuse mapAsync with async
  //async makes a component or a chain from akka stream to run on seperate actor
  //we learn how to use mapAsync to construct flows that calls external services
  // this is for every element we call a service that returns a future
}
