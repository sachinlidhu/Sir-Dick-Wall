package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object IntoAkkaConfig extends App {

  class SimpleLoggingActor extends Actor with ActorLogging{
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  /**
   * 1 - inline configuration
   */
  val configString =
    """
      |akka{
      | loglevel = "INFO"//"DEBUG"
      |}
      |""".stripMargin

  val config = ConfigFactory.parseString(configString)
  val system = ActorSystem("ConfigurationDemo", ConfigFactory.load(config))
  val actor = system.actorOf(Props[SimpleLoggingActor])

  actor ! "A message to remember"

  /**
   * 2-configuration file
   */
  val defaultConfigFileSystem = ActorSystem("DefaultConfigFileDemo")
  val defaultConfigActor = defaultConfigFileSystem.actorOf(Props[SimpleLoggingActor])
  defaultConfigActor ! "Rememeber me"

  /**
   * 3-separate config in the same file
   */

/*
  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig")
  val specialConfigSystem = ActorSystem("SpecialConfigDemo",specialConfig)
  val specialConfigActor = specialConfigSystem.actorOf(Props[SimpleLoggingActor])
  specialConfigActor ! "Remember me, I am special"
*/

  /**
   * 4 - seperate config in another file
   */

  val seperateConfig = ConfigFactory.load("secretFolder/secretConfiguration.conf")
  println(s"seperate config log level : ${seperateConfig.getString("akka.loglevel")}")

  /**
   * 5 - different file formats
   * JSON,properties
   */
/*

  val jsonConfig = ConfigFactory.load("json/jsonConfig.json")
  println(s"json config: ${jsonConfig.getString("aJsonProperty")}")
  println(s"json config: ${jsonConfig.getString("akka.loglevel")}")
*/

  val propsConfig = ConfigFactory.load("props/propsConfiguration.properties")
  println(s"props config: ${propsConfig.getString("my.simpleProperty ")}")
  println(s"props config: ${propsConfig.getString("akka.loglevel")}")
}
