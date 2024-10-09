package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App{

  //part1 - actor system
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  //[art2 - create actors...actors are unquiely identified, ,messages are asynchronous
  //crearte wod count actor
  class WordCountActor extends Actor{
    var totalWords = 0
    override def receive: Receive = {
      case message: String =>
        println(s"[word counter] I have received: $message")
        totalWords += message.split(" ").length
      case msg => println(s"[word counter] I cannot understand ${msg.toString}")
    }
  }
  //part3- instantiate our actor
  val  wordCounter = actorSystem.actorOf(Props[WordCountActor],"wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor])

  //part4 - communicate...u can only communicate with actor using actORrEF TYPE
  wordCounter ! "Hi I am learning Akka and its damn cool"
  anotherWordCounter ! "A different message"

  class Person(name: String) extends Actor{
    override def receive: Receive = {
      case "hi" => println(s"Hi,my name is $name")
      case _ =>
    }
  }
  val person = actorSystem.actorOf(Props(new Person("Bon"))) //this is legal but discouraged, best way to do this using companion object
  person ! "hi"

  object Person1{
    def props(name: String) = Props(new Person1(name))
  }
  class Person1(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"Hi,my name is $name")
      case _ =>
    }
  }

  val person1 = actorSystem.actorOf(Person1.props("Bob")) //this is legal but discouraged, best way to do this using companion object
  person1 ! "hi"
}
