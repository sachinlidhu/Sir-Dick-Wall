package akkaStreams_Part5_advanced

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, Attributes, FlowShape, Inlet, Outlet, SinkShape, SourceShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, GraphStageWithMaterializedValue, InHandler, OutHandler}

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Random, Success}

object CustomOperators extends App {

  implicit val system = ActorSystem("CustomOperators")
  implicit val materializer = ActorMaterializer

  //here we will learn how to create custom componenets usingan api known as graph stage

  //1 - create a custom source which emits random numers until cancelled
  class RandomNumberGenerator(max: Int) extends GraphStage[SourceShape[Int]] {

    val outPort = Outlet[Int]("randomGenerator")
    val random = new Random()

    override def shape: SourceShape[Int] = SourceShape(outPort)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
      //implement my logic here

      setHandler(outPort, new OutHandler {

        //below method will be called when there is demand from downstream
        override def onPull(): Unit = {
          //emit a new element
          val nextNumber = random.nextInt(max)
          //now push that element out of the outport
          push(outPort,nextNumber)
        }
      })
    }
  }

  val randomGeneratorSource = Source.fromGraph(new RandomNumberGenerator(100))
  randomGeneratorSource.runWith(Sink.foreach(println))

  //2 -create a custom sink that prints elements in  batches of a given size

  class Batcher(batchSize: Int) extends GraphStage[SinkShape[Int]] {

    val inPort = Inlet[Int]("batcher")

    override def shape: SinkShape[Int] = SinkShape[Int](inPort)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
      override def preStart(): Unit = {
        pull(inPort) //it will send first element and on pull will be triggererd
      }

      val batch = new mutable.Queue[Int]

      setHandler(inPort, new InHandler {
        override def onPush(): Unit = {
          val nextElement = grab(inPort)
          batch.enqueue(nextElement)

          if(batch.size >= batchSize) {
            println("New batch:" + batch.dequeueAll(_ => true).mkString("[",",","]"))
          }

          pull(inPort) //send demand upstream
        }

        override def onUpstreamFinish(): Unit = {
          if(batch.nonEmpty) {
            println("New batch:" + batch.dequeueAll(_ => true).mkString("[",",","]"))
            println("Stream finished")
          }
        }
      })
    }
  }
  val batcherSink = Sink.fromGraph(new Batcher(10))
  randomGeneratorSource.to(batcherSink).run()//this will cause deadlock as both sorce and sink wait for each other
  //to make it start use prestart method
  /**
   * exercise: create a custom flow - a simple filter flow
   * - for that u need 2 ports : an input port and an output port
   */
  class SimpleFilter[T](predicate: T => Boolean) extends GraphStage[FlowShape[T,T]] {

    val inPort = Inlet[T]("filterIn")
    val outPort = Outlet[T]("filterOut")

    override def shape: FlowShape[T, T] = FlowShape(inPort, outPort)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
      setHandler(outPort, new OutHandler {
        override def onPull(): Unit = pull(inPort)
      })
      setHandler(inPort, new InHandler {
        override def onPush(): Unit = {
        try {
          val nextElement = grab(inPort)

          if (predicate(nextElement)) {
            push(outPort, nextElement)
          } else {
            pull(inPort) //ask for another element
          }
        } catch {
          case e: Throwable => failStage(e)
        }
        }
      })
    }
  }
  val myFilter = Flow.fromGraph(new SimpleFilter[Int](_ > 5))
  randomGeneratorSource.via(myFilter).to(batcherSink).run()
  //backpressure lso works out of the box

  /**
   * materialized values in graph stages
   */
  //3 - create a flow that counts the number of elements that go through it
  class CounterFlow[T] extends GraphStageWithMaterializedValue[FlowShape[T,T], Future[Int]] {

    val inPort = Inlet[T]("counterInt")
    val outPort = Outlet[T]("counterOut")

    override val shape = FlowShape(inPort, outPort)

    override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[Int]) = {

      val promise = Promise[Int]
      val logic = new GraphStageLogic(shape) {
        //setting mutable state
        var counter = 0

        setHandler(outPort, new OutHandler {
          override def onPull(): Unit = pull(inPort)

          override def onDownstreamFinish(): Unit = {
            promise.success(counter)
            super.onDownstreamFinish()
          }
        })
        setHandler(inPort, new InHandler {
          override def onPush(): Unit = {
            //extract the element
            val nextElement = grab(inPort)
            counter += 1
            //pass it on
            push(outPort, nextElement)
          }

          override def onUpstreamFinish(): Unit = {
            promise.success(counter)
            super.onUpstreamFinish()
          }

          override def onUpstreamFailure(ex: Throwable): Unit = {
            promise.failure(ex)
            super.onUpstreamFailure(ex)
          }
        })
      }
      (logic, promise.future)
    }
  }
  val counterFlow = Flow.fromGraph(new CounterFlow[Int])
  val countFuture = Source(1 to 10)
    .viaMat(counterFlow)(Keep.right)
    .to(Sink.foreach[Int](println))
    .run()

  import system.dispatcher
  countFuture.onComplete {
    case Success(count) =>  println("The number of elements")
    case Failure(ex) => println(s"counting yhe elements: $ex")
  }
}
