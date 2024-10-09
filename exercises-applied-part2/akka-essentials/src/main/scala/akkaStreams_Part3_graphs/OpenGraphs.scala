package akkaStreams_Part3_graphs

import akka.actor.ActorSystem
import akka.stream.FlowShape
import akka.stream.scaladsl.{Broadcast, Flow}
//import akka.io.Udp.SO.Broadcast
import akka.stream.scaladsl.Sink
import akka.stream.{ActorMaterializer, SinkShape, SourceShape}
//import akka.stream.scaladsl.JavaFlowSupport.Sink
import akka.stream.scaladsl.{Concat, GraphDSL, Source}

object OpenGraphs extends App{

  implicit val system = ActorSystem("OpenGraphs")
  implicit val materializer = ActorMaterializer

  /*
  A composite source that concatenates 2 sources
  - emits ALL the elements from the first source
  - then all the elements from the second
   */
  val firstSource = Source(1 to 10)
  val secondSource = Source(42 to 1000)

  //step1
  val sourceGraph = Source.fromGraph(
    GraphDSL.create() {
      implicit builder =>
      import GraphDSL.Implicits._

        //step2
        val concat = builder.add(Concat[Int](2))

        //step3 : tying them together
        firstSource ~> concat
        secondSource ~> concat

        //STEP4
        SourceShape(concat.out)

    }
  )
  sourceGraph.to(Sink.foreach(println)).run()

  /*
  complex sink
   */
  val sink1 = Sink.foreach[Int](x => println(s"meningfuk thing 1: $x"))
  val sink2 = Sink.foreach[Int](x => println(s"meningfuk thing 2: $x"))

  //step 1
  val sinkGraph = Sink.fromGraph(
    GraphDSL.create() {
      implicit builder =>
        import GraphDSL.Implicits._

        //step 2 add a broadcast
        val broadcast = builder.add(Broadcast[Int](2))

        //step3 tie components together
        broadcast ~> sink1
        broadcast ~> sink2

        //step4
        SinkShape(broadcast.in)
    }
  )
  firstSource.to(sinkGraph).run()

  /**
   * write ur own flow that is composed of two other flows
   * -one that adds 1 to a number
   * -one that does number*10
   */
  val incrementer = Flow[Int].map(_+1)
  val multiplier = Flow[Int].map(_*10)

  val flowGraph = Flow.fromGraph(
    GraphDSL.create() {
      implicit builder =>
        import GraphDSL.Implicits._

        //step 2 - define auxiliary shapes
        val incrementerShape = builder.add(incrementer)//flow components can not be tied to with ~> operator, only shape type has its functionality
        val multiplierShape = builder.add(multiplier)//thats why we converted incrementer to incrementorSHape which is shape type
        //step 3 - connect the shapes
        //incrementer ~> multiplier ---wrong becasuse type is not shape
        incrementerShape ~> multiplierShape

        FlowShape(incrementerShape.in,multiplierShape.out)
    }
  )
  firstSource.via(flowGraph).to(Sink.foreach(println)).run()

  /**
   * would it be possible to create a flow from a sink and a source?
   *
   */
  def fromSinkAndSource[A,B](sink: Sink[A,_],source: Source[B,_]): Flow[A,B,_] =
    Flow.fromGraph(
      GraphDSL.create(){ implicit builder =>

        val sourceShape = builder.add(source)
        val sinkShape = builder.add(sink)

        //step3 -nothing
        //step4 - return thr shape
        FlowShape(sinkShape.in, sourceShape.out)
      }
    )
    val f = Flow.fromSinkAndSourceCoupled(Sink.foreach[String](println),Source(1 to 10))
  //above line is technically valid but problem here is there is no connection between components,
  // if the elements in sink go finished there is no way stopping source stream
  // if flow ends up connecting various part of the graph
  //thats why akka streams have special mthod Coupled that send termination and backpressure signals between these two
}
