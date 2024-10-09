package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

object ActorLoggingDemo extends App{
  class SimpleActorWithExplicitLogger extends Actor{
    val logger = Logging(context.system, this)
    override def receive: Receive = {
          /*
          logging is done on 4 levels
          debug,info,warn,error
           */
      case message => logger.info(message.toString)
    }
  }

  val system = ActorSystem("LoggingDemo")
  val actor = system.actorOf(Props[SimpleActorWithExplicitLogger])

  actor ! "Logging a simple message"

  //#2--most popular--actor logging
  class ActorWithLogging extends Actor with ActorLogging{
    override def receive: Receive = {
      case (a, b) => log.info(s"Two things: {} and {}", a, b)
      case message => log.info(message.toString)
    }
  }
  val simplerActor = system.actorOf(Props[ActorWithLogging])
  simplerActor ! "Logging a simple message by extending a trait"
  simplerActor ! (42, 65)
}
