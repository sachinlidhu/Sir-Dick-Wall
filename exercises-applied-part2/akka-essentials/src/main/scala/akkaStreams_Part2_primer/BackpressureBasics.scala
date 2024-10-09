package akkaStreams_Part2_primer

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object BackpressureBasics extends App{

  implicit val system = ActorSystem("BackpressureBasics")
  implicit val materializer = ActorMaterializer

  //backpressure is all about slowing down a fast producer in presence of slow consumer
  val fastSource = Source(1 to 1000)
  val slowSink = Sink.foreach[Int] {
    x =>
      //simulate a long processing
      Thread.sleep(1000)
      println(s"Sink: $x")
  }
  fastSource.to(slowSink).run() //not backpressure

  fastSource.async.to(slowSink).run() //backpressure
  val simpleFlow = Flow[Int].map {
    x =>
      println(s"Incoming: $x")
      x+1
  }
  fastSource.async.via(simpleFlow).async.to(slowSink).run()

  /*
  component will react to backpressure in following ways:
  -try to slow down if possible
  - buffer element until there is more demand
  -drop down elements from the buffer if it overflows
  -tear down/kill the whole stream (failure)
   */
  val bufferedFlow = simpleFlow.buffer(10, overflowStrategy = OverflowStrategy.dropHead) //drophead means to drop oldest element in buffere
  fastSource.async
    .via(bufferedFlow).async
    .to(slowSink)
    .run()
  /*
  overflow strategies:
  - drop head = oldest
  - drop tail = newest
  -drop new = exect element to be added = keeps the buffer
  -drop the entire buffer
  etc
   */

  //throttling -- manually trigger backpressure
  fastSource.throttle(2, 1 second).runWith(Sink.foreach(println)) //it will emit 2 elements per second
}
