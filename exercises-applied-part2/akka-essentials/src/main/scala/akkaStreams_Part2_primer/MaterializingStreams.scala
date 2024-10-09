package akkaStreams_Part2_primer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.util.{Failure, Success}

object MaterializingStreams extends App{
  implicit val system = ActorSystem("MaterializingStreams")
  implicit val materializer = ActorMaterializer //Its one of the objects that is giving resources for running akka streams
  import system.dispatcher
  val simpleGraph = Source(1 to 10).to(Sink.foreach(println))
  //all the components inside a graph can have different materialised value
  // but the result of running a grpah has singlr materialoisd value
  // and u want to choose which value u want to retreive at the end
  // so when we construct a graph with via and to
  // by default left most materialised value is given preference and is pickd
  // in our case source is left most, so iyts materialised value will be picked up

  val simpleMaterialozedValue = simpleGraph.run()

  val source = Source(1 to 10)
  val sink = Sink.reduce[Int]((a,b) => a+b)
  val sumFuture = source.runWith(sink) //runWith has inbuilt run()
  sumFuture.onComplete {
    case Success(value) => println(s"the sum of all elements is: $value")
    case Failure(ex) => println(s"exception------$ex")
  }

  // But we can have control on which materialised value we can choose by using different methods
  //choosing materailised value
  val simpleSource = Source(1 to 10)
  val simpleFlow = Flow[Int].map(_+1)
  val simpleSink = Sink.foreach(println)
  val graph = simpleSource.viaMat(simpleFlow)(Keep.right).toMat(simpleSink)(Keep.right)
  //viaMat and keep to make materialise value of ur choice
  //it will give materialised value of extreme right ehich is sink in our case
  graph.run().onComplete {
    case Success(_) => println(" stream processing finished")
    case Failure(exception) => println(s" failed with $exception")
  }

  //sugars
 /* val sum = Source(1 to 10).runWith(Sink.reduce(_+_)) //source.to(Sink.reduce)(Keep.right)
  val sum1 = Source(1 to 10).runReduce(_+_) // EVEN SHORTER
*/
  //backwards
  Sink.foreach[Int](println).runWith(Source.single(42)) //source(..).to(sink...).run()

  //both ways
  Flow[Int].map(x => 2 * x).runWith(simpleSource, simpleSink)

  /**
   * return the last element out of the source (use sink.last)
   * compute the total word count out of a stream of sentences
   * for that use map (operation available on flow),
   * fold (operation available on flow and a sink) reduce (operation available on both as well)
   */

  val f1 = Source(1 to 10).toMat(Sink.last)(Keep.right).run().onComplete {
    case Success(x) => println(s"$x")
  }

  val f2 = Source(1 to 10).runWith(Sink.last) //alternative

  val sentenceSource = Source(List(
    "Akka is awesome",
    "I love streams",
    "Materialized values are killing me"
  ))
  val wordCountSink = Sink.fold[Int, String](0)(
    (currentWords, newSentence) => currentWords + newSentence.split(" ").length
  )
  val g1 = sentenceSource.toMat(wordCountSink)(Keep.right).run()
  val g2 = sentenceSource.runWith(wordCountSink)
  val g3 = sentenceSource.runFold(0)((currentWords, newSentence) =>
    currentWords + newSentence.split(" ").length)

  val wordCountFlow = Flow[String].fold[Int](0)(
    (currentWords, newSentence) => currentWords + newSentence.split(" ").length
  )
  val g4 = sentenceSource.via(wordCountFlow).toMat(Sink.head)(Keep.right).run()
  val g5 = sentenceSource.viaMat(wordCountFlow)(Keep.left).toMat(Sink.head)(Keep.right).run()
  val g6 = sentenceSource.via(wordCountFlow).runWith(Sink.head)
  val g7 = wordCountFlow.runWith(sentenceSource, Sink.head)._2.onComplete{
    case Success(x) => println(s"$x")
  }
}
