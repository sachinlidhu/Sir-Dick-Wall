package akkaStreams_Part2_primer

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object OperatorFusion extends App{

  implicit val system = ActorSystem("OperatorFusion")
  implicit val materializer = ActorMaterializer

  val simpleSource = Source(1 to 1000)
  val simpleFlow = Flow[Int].map(_+1)
  val simpleFlow2 = Flow[Int].map(_*10)
  val simpleSink = Sink.foreach[Int](println)

  //simpleSource.via(simpleFlow).via(simpleFlow2).to(simpleSink).run()
  //this is a valid stream graph...but it runs on the same actor
  //this is called operator/component fusion...it is done to increase throughput
  //single cpu core is usd

  //its like writing above graph in below manner

  class SimpleActor extends Actor{
    override def receive: Receive = {
      case x:Int =>
        val x2 = x+1 //flow1
        val y = x2*10 //flow2
        println(y) //sink
    }
  }
  val simpleActor = system.actorOf(Props[SimpleActor])
  //(1 to 10).foreach(simpleActor ! _)

  //complex operators
  //complex flow:
  val complexFlow = Flow[Int].map {
    x =>
      //simulating long computation
      Thread.sleep(1000)
      x+1
  }
  val complexFlow2 = Flow[Int].map {
    x =>
      //simulating long computation
      Thread.sleep(1000)
      x*10
  }
  simpleSource.via(complexFlow).via(complexFlow2).to(simpleSink).run()

  //now to make graph run parallelly om multiple actors we use async boundary
  //async boundary
  simpleSource.via(complexFlow).async //runs on one seperate actor
    .via(complexFlow2).async //runs on another actor
    .to(simpleSink) //runs on third actor
    .run()

  //ordering guarantees
  //without async we will get full ordering
  Source(1 to 3)
    .map(element => { println(s"Flow A: $element"); element})
    .map(element => { println(s"Flow B: $element"); element})
    .map(element => { println(s"Flow C: $element"); element})
    .runWith(Sink.ignore)

  //with async ...but for ordring will maintained for number according to Source.
  Source(1 to 3)
    .map(element => {
      println(s"Flow A: $element"); element
    }).async
    .map(element => {
      println(s"Flow B: $element"); element
    }).async
    .map(element => {
      println(s"Flow C: $element"); element
    }).async
    .runWith(Sink.ignore)
}
