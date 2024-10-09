package akkaStreams_Part5_advanced

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, BroadcastHub, Keep, MergeHub, Sink, Source}
import akka.stream.{ActorMaterializer, KillSwitches}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object DynamicStreamHandling extends App{
  implicit val system = ActorSystem("DynamicStreamHandling")
  implicit val materializer = ActorMaterializer
  import system.dispatcher
  //1-kill switch--it is used to terminate the stream at any point of time
  val killSwitchFlow = KillSwitches.single[Int]
  val counter = Source(LazyList.from(1)).throttle(1, 1 second).log("counter")
  val sink = Sink.ignore
  val killSwitch = counter.viaMat(killSwitchFlow)(Keep.right).toMat(sink)(Keep.left).run()

  system.scheduler.scheduleOnce(3 seconds){
    killSwitch.shutdown()
  }
  val anotherCounter = Source(LazyList.from(1)).throttle(2, 1 second)
  val sharedKillSwitch = KillSwitches.shared("oneButtonToRuleThemAll")

  counter.via(sharedKillSwitch.flow).runWith(Sink.ignore)
  anotherCounter.via(sharedKillSwitch.flow).runWith(Sink.ignore)

  system.scheduler.scheduleOnce(3 seconds) {
    sharedKillSwitch.shutdown()
  }
  //merge hub
  val dynamicMerge = MergeHub.source[Int] // here source will materialize to sink
  val materializedSink = dynamicMerge.to(Sink.foreach[Int](println)).run()

  //use this sink anytime we like
  Source(1 to 10).runWith(materializedSink)
  counter.runWith(materializedSink)

  //Broadcast hub --opposite to mergehub
  val dynamicBroadcast = BroadcastHub.sink[Int]
  val materializedSource = Source(1 to 100).runWith(dynamicBroadcast)

  materializedSource.runWith(Sink.ignore)
  materializedSource.runWith(Sink.foreach[Int](println))

  /**
   * exercise - combine a mergeHub and a broadcastHub
   *
   * A publisher-subscriber component ---u can dynamically add sources and sink
   */
  val merge = MergeHub.source[String]
  val bcast = BroadcastHub.sink[String]
  val (publisherPort, subscriberPort) = merge.toMat(bcast)(Keep.both).run()

  subscriberPort.runWith(Sink.foreach(e => println(s"i received: $e")))
  subscriberPort.map(string => string.length).runWith(Sink.foreach(n => println(s"I got the number : $n")))

  Source(List("Akka","is","amazing")).runWith(publisherPort)
  Source.single("streammsss").runWith(publisherPort)
}
