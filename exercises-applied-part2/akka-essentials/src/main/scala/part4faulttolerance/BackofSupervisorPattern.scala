package part4faulttolerance

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.{Backoff, BackoffSupervisor}

import java.io.File
import scala.io.Source
import scala.sys.Prop
//import scala.reflect.io.File

object BackofSupervisorPattern extends App{

  case object ReadFile
  class FileBasedPersistentActor extends Actor with ActorLogging {

    var dataSource: Source = null

    override def preStart(): Unit =
      log.info("Persistent actor starting")

    override def postStop(): Unit =
      log.warning("Persistent actor has stopped")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit =
      log.warning("Persistent actor restrting")

    override def receive: Receive = {
      case ReadFile =>
        if (dataSource == null) {
          dataSource = Source.fromFile(new File("src/resouces/testfiles/important.txt"))
        }
        log.info(s"I have just read some important data: "+ dataSource.getLines().toList)
    }
  }

  val system = ActorSystem("BackoffSupervision")
  val simpleActor = system.actorOf(Props[FileBasedPersistentActor])
  simpleActor ! ReadFile

/*  val simpleSupervisorProps = BackoffSupervisor.props(
    Backoff.onFailure()
  )*/
}
