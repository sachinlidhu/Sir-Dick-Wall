package part6patterns

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}

object StashDemo extends App {

  /*
  lets say we have a actor called resource actor which has a access to some resources like a file or like that.
  if this actor is open it can receive read/write requests to the resource
  otherwise it will postpone all read and write requests until the state is open
   */
  //assume wgen the actor starts its in close state
  /*
  when it receives Open message it will switch to open state
  and then it has access to read and write message
   */

  /*
  if we are receiving messages in the order like [open, read, read, write]
  then directly switch to open state
  read the data
  read the data again
  write the data

  but if we receive the messages in the order like [read, open, write]
  then stash the read--it means save it in stash and postponed it, wait for open messsage
  once we receive open message, switch to the open state, and that stash is prepended to the mail box.
  In the mailbox, after open, we have write message only and in the stash we have read  so prepend the stash to mailbox
  now mailbox is like [read, write]
  now read and write are handled in order
   */
  case object Open
  case object Close
  case object Read
  case class Write(data: String)

  //step 1 - mix in the stash trait
  class ResourceActor extends Actor with ActorLogging with Stash {
    private var innerData: String = ""

    override def receive: Receive = closed

    def closed: Receive = {
      case Open =>
        log.info("Opening resource")
        // step 3 - unstashAll whn u switch the message handler
        unstashAll()
        context.become(open)
      case message =>
        log.info(s"stashing $message because I cant handle it in the closed state")
        //step 2 - stash away what u cant handle
        stash()
    }

    def open:Receive = {
      case Read =>
        log.info(s"I have read $innerData")
      case Write(data) =>
        log.info(s"I am writing $data")
        innerData = data
      case Close =>
        log.info(s"closing the resource")
        unstashAll()
        context.become(closed)
      case message =>
        log.info(s"stashing $message because I cant handle it in the closed state")
        stash()
    }
  }
  val system = ActorSystem("StashDemo")
  val resourceActor = system.actorOf(Props[ResourceActor])
  resourceActor ! Write("I love stash")
  resourceActor ! Read
  resourceActor ! Open
}
