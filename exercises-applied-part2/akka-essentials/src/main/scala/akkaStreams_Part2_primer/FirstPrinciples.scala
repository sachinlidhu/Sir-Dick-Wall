package akkaStreams_Part2_primer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Future
import scala.math.Fractional.Implicits.infixFractionalOps
import scala.math.Integral.Implicits.infixIntegralOps
import scala.math.Numeric.BigDecimalAsIfIntegral.mkNumericOps

object FirstPrinciples extends App{

  implicit val system = ActorSystem("FirstPrinciples")
  implicit val materializer = ActorMaterializer

  //every akka stream starts with a source
  val source = Source(1 to 10)

  //every akka stream ends with a sink
  val sink = Sink.foreach[Int](println)

  //in order to create akka stream we need to connect source and sink
  val graph = source.to(sink)
 // graph.run

  //flows transform elements
  val flow = Flow[Int].map(x => x+1)

  // flow is attached using via
  val sourceWithFlow = source.via(flow)
  val flowWithSink = flow.to(sink)

  /*sourceWithFlow.to(sink).run()
  //or
  source.to(flowWithSink).run()
  //or
  source.via(flow).to(sink).run()*/

  //nulls are not allowed
  /*val illegalSource = Source.single[String](null)
  illegalSource.to(Sink.foreach(println)).run()*/
  // above will give null pointer exception
  //But we know in scala we can deal with nulls using Options

  //various kinds of sources
  val finiteSource = Source.single(1)
  val anotherFiniteSource = Source(List(1,2,3))
  val emptySource = Source.empty[Int]
  val infiniteSource = Source(LazyList.from(1))//.to(sink).run()
  import scala.concurrent.ExecutionContext.Implicits.global
  val futureSource = Source.future(Future(42))

  //various kinds of sinks
  val theMostBoringSink = Sink.ignore
  val foreachSink = Sink.foreach[String](println)
  val headSink = Sink.head[Int] //retrieves head and then closed the stream
  val foldSink = Sink.fold[Int, Int](0)((a,b) => a + b)

  //kinds of flows - usually mapped to collection operators
  val mapFlow = Flow[Int].map(x => 2 * x)
  val takeFlow = Flow[Int].take(5) //it will only retrieve only first 5 elements
  //we have drop,filter but dont have `flatmap`

  //source -> flow -> flow -> ... -> sink
  val doubleFlowGraph = source.via(mapFlow).via(takeFlow).to(sink)
  doubleFlowGraph.run()

  //akka stream is very rich in syntactic sugars
  val mapSource = Source(1 to 10).map(x => x * 2)
  // eqvivalent to Source(1 to 10).via(Flow[Int].map(x => x * 2))

  // akkaStreams have very nice api to run streams direcytly
  mapSource.runForeach(println) // mapSource.to(Sink.foreach[Int](println)).run()

  //operators = components

  /**
   * Exercise-
   * create a stream that takes the names of persons, then you will keep the first 2 names with length > 5 chars
   *
   */
  val names = List("Alice","Bob","Charlie","David","Martin","AkkaStreams")
  val nameSource = Source(names)
  val longNameFlow = Flow[String].filter(name => name.length > 5)
  val limitFlow = Flow[String].take(2)
  val nammeSink = Sink.foreach[String](println)
  nameSource.via(longNameFlow).via(limitFlow).to(nammeSink).run()

  //another way
  nameSource.filter(_.length > 5).take(2).runForeach(println)

}
