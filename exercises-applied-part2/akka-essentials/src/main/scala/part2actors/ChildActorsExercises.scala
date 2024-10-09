package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActorsExercises extends App{
  //distributed word counting
  object WordCounterMaster{
    case class Initialize(nChildren: Int)
    case class WordCountTask(id:Int, text: String)
    case class WordCountReply(id:Int,count: Int)
  }
  class WordCounterMaster extends Actor{
    import WordCounterMaster._
    override def receive: Receive = {
      case Initialize(nChildren)=>
        println(s"[master] initializing...")
        val childrenRefs = for(i <- 1 to nChildren) yield context.actorOf(Props[WordCounterWorker],s"wcw+$i")
        context.become(withChildren(childrenRefs, 0, 0, Map()))
    }
    def withChildren(childrenRefs: Seq[ActorRef], currentChildIndex: Int, currentTaskId: Int, requestMap: Map[Int,ActorRef]): Receive ={
      case text: String =>
        println(s"[master] iu have : $text - i will send it to $currentChildIndex")
        val originalSender = sender()
        val task = WordCountTask(currentTaskId, text)
        val childRef = childrenRefs(currentChildIndex)
        childRef ! task
        val nextChildIndex = (currentChildIndex + 1)% childrenRefs.length
        val newTaskId = currentTaskId + 1
        val newRequestMap = requestMap + (currentTaskId -> originalSender)
        context.become(withChildren(childrenRefs, nextChildIndex, newTaskId, newRequestMap))
      case WordCountReply(id, count) =>
        println(s"[mastr] i have received a reply for task id $id with $count")
        val originalSender = requestMap(id)
        originalSender ! count
        context.become(withChildren(childrenRefs, currentChildIndex, currentTaskId, requestMap - id))
    }
  }

  class WordCounterWorker extends Actor{
    import WordCounterMaster._
   override def receive:Receive = {
     case WordCountTask(id,text) =>
       println(s"${self.path} i have received a task $id with $text")
       sender() ! WordCountReply(id, text.split(" ").length)
   }
  }

  class TestActor extends Actor{
    import WordCounterMaster._
    override def receive: Receive = {
      case "go" =>
        val master = context.actorOf(Props[WordCounterMaster],"master")
        master ! Initialize(3)
        val texts = List("I love Akka"," Scala is super dope", "yes", "mre too")
        texts.foreach(text => master ! text)
      case count: Int =>
        println(s"[test actor] I received a reply: $count")
    }
  }
  val system = ActorSystem("roundrobinWordCount")
  val testActor = system.actorOf(Props[TestActor],"testActor")
  testActor ! "go"
  /*
  flow---
  send initialize(10) to wordcounterMaster
  then it will create 10 workers
  if i send `akka is awasome ` to wordcounter,aster then wcM will send a wordcountTask("..") to one of its children
  and child child replies with a wordcountreply(3) to master... as 3 is the word count..
  and then master replies with 3  to the sender

  requester -> wcm ->wcw
           r <- wcm <-
   */
  /*
  to achieve load balancing between children popular and simple approach is round robin logic..
  that is task will be send to workers in order, we will schedule the order
   */
}
