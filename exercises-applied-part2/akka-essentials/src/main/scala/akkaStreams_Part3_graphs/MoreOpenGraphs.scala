package akkaStreams_Part3_graphs

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, FanOutShape2, UniformFanInShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, ZipWith}

import java.util.Date

object MoreOpenGraphs extends App{

  implicit val system = ActorSystem("MoreOpenGraphs")
  implicit val materializer = ActorMaterializer

  /*
  Example- max3 operator
  - this operator has 3 inputs of type int
  -whenever i have 3 values in its input this component will push out max of the 3
  -how would we design that
   */
  //step 1
  val max3StaticGraph = GraphDSL.create() {
    implicit builder =>
      import GraphDSL.Implicits._

      //step 2 - define aux shapes
      val max1 = builder.add(ZipWith[Int, Int, Int]((a, b) => Math.max(a,b)))
      val max2 = builder.add(ZipWith[Int, Int, Int]((a, b) => Math.max(a,b)))

      //step 3
      max1.out ~> max2.in0

      //step 4
      UniformFanInShape(max2.out, max1.in0, max1.in1, max2.in1)
  }

  val source1 = Source(1 to 10)
  val source2 = Source(1 to 10).map(_ => 5)
  val source3 = Source((1 to 10).reverse)

  val maxSink = Sink.foreach[Int](x => println(s"Max is: $x"))

  val max3RunnableGraph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val max3shape = builder.add(max3StaticGraph)

      source1 ~> max3shape.in(0)
      source2 ~> max3shape.in(1)
      source3 ~> max3shape.in(2)
      max3shape.out ~> maxSink

      ClosedShape
    }
  )
  max3RunnableGraph.run()
  //same for uniformFanOutShape
  //Uniform... means all inputs / outputs are of same type

  //lets see now Non-uniform fan out shape -- here we deals with different type outputs anmd inputs

  case class Transaction(id: String, source: String, recipient: String, amount: Int, date: Date)

  val transactionSourc = Source(List(
    Transaction("12345","Paul","Jim",100, new Date),
    Transaction("12354","Daniel", "Jim", 100000, new Date),
    Transaction("23451", "Jim", "Alice", 7000, new Date)
  ))
  val bankProcessor = Sink.foreach[Transaction](println)
  val suspiciousAnalysisService = Sink.foreach[String](txnId => println(s"Suspicious transaction ID: $txnId"))

  val suspiciousTxnStaticGraph = GraphDSL.create(){
    implicit builder =>
      import GraphDSL.Implicits._

      val broadcast = builder.add(Broadcast[Transaction](2))
      val suspiciousTxnFilter = builder.add(Flow[Transaction].filter(txn => txn.amount > 10000))
      val txnIdExtractor = builder.add(Flow[Transaction].map[String](txn => txn.id))

      broadcast.out(0) ~> suspiciousTxnFilter ~> txnIdExtractor

      new FanOutShape2(broadcast.in, broadcast.out(1), txnIdExtractor.out)
  }

  val suspiciousTxnRunnableGraph = RunnableGraph.fromGraph(
    GraphDSL.create() {
      implicit builder =>
        import GraphDSL.Implicits._

        val suspiciousTxnShape = builder.add(suspiciousTxnStaticGraph)

        transactionSourc ~> suspiciousTxnShape.in
        suspiciousTxnShape.out0 ~> bankProcessor
        suspiciousTxnShape.out1 ~> suspiciousAnalysisService

        ClosedShape
    }
  )

  suspiciousTxnRunnableGraph.run()
}
