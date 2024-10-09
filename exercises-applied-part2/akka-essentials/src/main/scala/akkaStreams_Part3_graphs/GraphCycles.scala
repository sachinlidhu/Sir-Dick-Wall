package akkaStreams_Part3_graphs

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, Graph, UniformFanInShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, MergePreferred, RunnableGraph, Sink, Source, Zip}

object GraphCycles extends App{

  implicit val system = ActorSystem("GraphCycles")
  implicit val materializer = ActorMaterializer

  val accelerator = GraphDSL.create() {
    implicit builder =>
      import GraphDSL.Implicits._

      val sourceShape = builder.add(Source(1 to 100))
      val mergeShape = builder.add(Merge[Int](2))
      val incrementerShape = builder.add(Flow[Int].map { x =>
        println(s"Accelerating $x")
        x + 1
      })
      sourceShape ~> mergeShape ~> incrementerShape
                     mergeShape <~ incrementerShape
      ClosedShape
  }
  //RunnableGraph.fromGraph(accelerator).run()
  //here we will get into situation called `graph cycle deadlock`
  /*
  - if u have cycles in your graph, you risk deadlocking
   */
  /*
  solutions
  1--MergePreferred
  2--buffers --- val incrementerShape = builder.add(Flow[Int].buffer(10, OverflowStrategy.dropHead).map{..}
   */
  /**
   * exercise
   * create a fan-in shape which takes 2 inputs which will be fed with exactly 1 number
   * and the output will emit an infinite fibonacci sequence based off those 2 numbers
   * Hint: use zipWith and cycles, mergePreffered
   */
  val fibonacciGenerator = GraphDSL.create(){ implicit builder =>
    import GraphDSL.Implicits._

    val zip = builder.add(Zip[BigInt, BigInt])
    val mergePreferred = builder.add(MergePreferred[(BigInt, BigInt)](1))
    val fiboLogic = builder.add(Flow[(BigInt, BigInt)].map {pair =>
      val last = pair._1
      val previous = pair._2

      Thread.sleep(100)

      (last + previous, last)
    })
    val broadcast = builder.add(Broadcast[(BigInt, BigInt)](2))
    val extractLast = builder.add(Flow[(BigInt, BigInt)].map(pair => pair._1))

    zip.out ~> mergePreferred ~> fiboLogic ~> broadcast ~> extractLast
               mergePreferred.preferred <~    broadcast

    UniformFanInShape(extractLast.out, zip.in0,zip.in1)
  }

  val fiboGraph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder =>
      import  GraphDSL.Implicits._

      val source1 = builder.add(Source.single[BigInt](1))
      val source2 = builder.add(Source.single[BigInt](1))
      val sink = builder.add(Sink.foreach[BigInt](println))
      val fibo = builder.add(fibonacciGenerator)

      source1 ~> fibo.in(0)
      source2 ~> fibo.in(1)
      fibo.out ~> sink

      ClosedShape
    }
  )

  fiboGraph.run()
}
