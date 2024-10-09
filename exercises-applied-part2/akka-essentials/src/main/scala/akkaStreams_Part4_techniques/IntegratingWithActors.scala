package akkaStreams_Part4_techniques

//import akka.actor.Nobody.!
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging.StandardOutLogger.!
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout
//import org.scalatest.FailureMessages.seconds

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object IntegratingWithActors extends App{

  implicit val system = ActorSystem("IntegratingWithActor")
  implicit val materializer = ActorMaterializer

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case s: String =>
        log.info(s"just received a string: $s")
        sender() ! s"$s$s"
      case n:Int =>
        log.info(s"just received a number: $n")
        sender() ! (2 * n)
      case _ =>
    }
  }
  val simpleActor = system.actorOf(Props[SimpleActor],"simpleActor")

  val numberSource = Source(1 to 10)

  //actor as a flow
  implicit val timeout = Timeout(2 seconds)
  val actorBasedFlow = Flow[Int].ask[Int](parallelism = 4)(simpleActor)

  numberSource.via(actorBasedFlow).to(Sink.ignore).run()

  //equivalent way to do above is
  //numberSource.ask[Int](parallelism = 4)(simpleActor).to(Sink.ignore).run()

  /*
  Actor as a source
   */
  val actorPoweredSource = Source.actorRef[Int](bufferSize = 10, overflowStrategy = OverflowStrategy.dropHead)
  val materializedActorRef = actorPoweredSource.to(Sink.foreach[Int](number => println(s"Actor powered flow got number: $number")))
  //materializedActorRef ! 10
  //terminate the stream
 // materializedActorRef ! akka.actor.Status.Success("complete")
  /*
  Actor as a destination/sink
  ---for that we have to support following
  -an init message
  - an ack message to confirm the reception
  - a complete message
  - a function to generate a message in case the stream throws an exception
   */

  case object StreamInit
  case object StreamAck
  case object StreamComplete
  case class StreamFailed(ex: Throwable)

  class DestinationActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case StreamInit =>
        log.info("Stream initialized")
        sender() ! StreamInit

      case StreamComplete =>
        log.info("Stream complete")
        context.stop(self)

      case StreamFailed(ex) =>
        log.warning(s"Stream failed: $ex")

      case message =>
        log.info(s"Message $message has come to its fainal resting point.")
        sender() ! StreamAck
    }
  }
  val destinationActor = system.actorOf(Props[DestinationActor], "destinationActor")

  val actorPoweredSink = Sink.actorRefWithBackpressure[Int](
    destinationActor,
    onInitMessage = StreamInit,
    onCompleteMessage = StreamComplete,
    ackMessage = StreamAck,
    onFailureMessage = throwable => StreamFailed(throwable) // optional
  )

  Source(1 to 10).to(actorPoweredSink).run()

  //Sink.actorRef

}
