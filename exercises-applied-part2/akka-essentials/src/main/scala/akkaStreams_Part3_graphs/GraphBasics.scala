package akkaStreams_Part3_graphs

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.javadsl.GraphDSL.builder
import akka.stream.scaladsl.{Balance, Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source, Zip}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object GraphBasics extends App{

  implicit val system = ActorSystem("GraphBasics")
  implicit val materializer = ActorMaterializer

  val input = Source(1 to 100)
  val incrementer = Flow[Int].map(_+1) //hard computatio
  val multiplier = Flow[Int].map(_*10) //hard computation

  //i want to perform execution in parrallel and throw result in tuple
  val output = Sink.foreach[(Int,Int)](println)

  //setting the fundamentals for thr graph
  val graph = RunnableGraph.fromGraph(
    GraphDSL.create() {
      implicit builder: GraphDSL.Builder[NotUsed] =>
        import GraphDSL.Implicits._ //brings ome nice operators into scope

        //add neccessary componenet to the graph
          val broadcast = builder.add(Broadcast[Int](2)) //fan-out operator
          val zip = builder.add(Zip[Int, Int]) // fan -in operator because it has 2 input and 1 output

          //tying up the components
          input ~> broadcast //input feeds into broadcast

          broadcast.out(0) ~> incrementer ~> zip.in0
          broadcast.out(1) ~> multiplier ~> zip.in1

          zip.out ~> output

          //return a closed shaped to make type as Shape
        ClosedShape
      //actually the builder which we are using above is mutable
      // so at last we make it immutable using Closed shape and returns shape type
          //it will be of shape type
    }//step 2 -now convert to graph
  )//step3 - now convert to runnable graph so that run method can work

  graph.run() //step 4 -run the graph and materialise it

  /**
   * exerecise 1 -feed a source into 2 sinks at the same time (hint- use a broadcast)
   */
  val firstSink = Sink.foreach[Int](x => println(s"First sink: $x"))
  val secondSink = Sink.foreach[Int](x => println(s"First sink: $x"))

  //step 1
  val sourceToTwoSinksGraph = RunnableGraph.fromGraph(
    GraphDSL.create() {
      implicit  builder =>
        import GraphDSL.Implicits._

        //step2 - declaring components
        val broadcast = builder.add(Broadcast[Int](2))
        //step3 - tying up the componenets
        input ~> broadcast
        broadcast.out(0) ~> firstSink
        broadcast.out(1) ~> secondSink
        //another way to do that is
       /* input ~> broadcast ~>firstSink //this is calles implicit port numbering
                 broadcast ~>secondSink*/
        //step4
        ClosedShape
    }
  )

  /**
   * exercise 2 -
   * two sources - fast source and slow source----merge them and apply some balance then load to 2 sinks
   */
  val fastSource = input.throttle(5, 1 second)
  val slowSource = input.throttle(2, 1 second)

  val sink1 = Sink.foreach[Int](x => println(s"Sink1 : $x"))
  val sink2 = Sink.foreach[Int](x => println(s"Sink1 : $x"))

  val balanceGraph = RunnableGraph.fromGraph {
    GraphDSL.create(){
      implicit builder =>
        import GraphDSL.Implicits._
        val merge = builder.add(Merge[Int](2))
        val balance = builder.add(Balance[Int](2))

        fastSource ~> merge ~>balance ~> sink1
        slowSource ~> merge
        balance ~> sink2

          ClosedShape
    }
  }
  balanceGraph.run()
}
