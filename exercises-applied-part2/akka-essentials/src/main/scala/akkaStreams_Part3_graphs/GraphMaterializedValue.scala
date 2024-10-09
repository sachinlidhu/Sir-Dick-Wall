package akkaStreams_Part3_graphs

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, FlowShape, SinkShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Sink, Source}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object GraphMaterializedValue extends App{
  implicit val system = ActorSystem("GraphMaterializedValues")
  implicit val materializer = ActorMaterializer

  val wordSource = Source(List("Akka", "is", "awesome", "rock", "the", "jvm"))
  val printer = Sink.foreach[String](println)
  val counter = Sink.fold[Int,String](0)((count, _) => count + 1)
  /*
  A composite component (sink)
  - prints out all strings which are lowercase
  - counts the strings that are short (< 5 chars)
   */

  val complexWordSink = Sink.fromGraph(
    GraphDSL.createGraph(counter) { implicit builder => counterShape =>
      import GraphDSL.Implicits._

      val broadcast = builder.add(Broadcast[String](2))
      val lowercaseFilter = builder.add(Flow[String].filter(word => word == word.toLowerCase))
      val shortStrungFilter = builder.add(Flow[String].filter(_.length < 5))

      broadcast ~> lowercaseFilter ~> printer
      broadcast ~> shortStrungFilter ~> counterShape

      SinkShape(broadcast.in)

    }
  )
  /*
  if you hover over `complexWordSink` ot will be of type Sink[String, NotUsed]
  But if we want to have custom type other than NotUsed, I mean use different materialized value then
  we need to pass that materialized value as a first parameter in Create method (use CreateGraph method instead), which we generally kept empty previously.
  and use one more value after implicit builder
   */

  import  system.dispatcher
  val shortStringCountFuture = wordSource.toMat(complexWordSink)(Keep.right).run()
  shortStringCountFuture.onComplete {
    case Success(count) => println(s"The total number of short string is: $count")
    case Failure(exception) => println(s"The failure: $exception")
   }

  /*
  Now what if we neeed to use multiple materialized values
  above we used only one which was `counter`
  lets say we need both printer and counter
  for that use below syntax
  ---------
    GraphDSL.createGraph(printer,counter)((printerMatValue, counterMatValue) => counterMatValue) {
     implicit builder => (printerShape,counterShape) =>
   */
  /**
   * erercise
   * def enhanceFlow[A,B](flow: Flow[A,B,_]): Flow[A,B, Future[Int]] = ???
   *
   * implement above methid which takes flow from A to B with any materialized value
   * and return another flow from A to B with materuialized value of Future[Int]
   * and this Future Int contains number if elements that went through that Future[Int]
   *
   * hint: use broadcast and sink.fold
   */
  def enhanceFlow[A,B](flow: Flow[A,B,_]): Flow[A,B, Future[Int]] = {
    val counterSink = Sink.fold[Int,B](0)((count,_) => count + 1)

    Flow.fromGraph(
      GraphDSL.createGraph(counterSink){
        implicit builder =>counterSinkShape =>
          import GraphDSL.Implicits._

          val broadcast = builder.add(Broadcast[B](2))
          val originalFlowShape = builder.add(flow)

          originalFlowShape ~> broadcast ~> counterSinkShape

          FlowShape(originalFlowShape.in, broadcast.out(1))
      }
    )
  }
  val simpleSource = Source(1 to 42)
  val simpleFlow = Flow[Int].map(x => x)
  val simpleSink = Sink.ignore

  val enhancedFlowCountFuture = simpleSource.viaMat(enhanceFlow(simpleFlow))(Keep.right).toMat(simpleSink)(Keep.left).run()
  enhancedFlowCountFuture.onComplete {
    case Success(count) => println(s"$count elements went through enhanved flow")
    case _ => println("Something wrong")
  }
}
