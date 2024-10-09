package part6patterns

import akka.actor.Actor.Receive
import akka.pattern.pipe

import scala.util.{Failure, Success}
//import akka.pattern.StatusReply.Success
//import akka.actor.Status.Success
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.scalatest.BeforeAndAfterAll
//import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class AskSpec extends TestKit(ActorSystem("AskSpec"))
  with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import AskSpec._

  "An authenticator" should {
    import AuthManager._
    "fail to authenticate a non-registered user" in {
      val authManager = system.actorOf(Props[AuthManager])
      authManager ! Authenticate("daniel","rtjvm")
      expectMsg(AuthFailure(AUTH_FAILURE_NOT_FOUND))
    }
    "fail to authenticate if invalid password" in {
      val authManager = system.actorOf(Props[AuthManager])
      authManager ! RegisterUser("daniel","rtjvm")
      authManager ! Authenticate("daniel","iloveakka")
      expectMsg(AuthFailure(AUTH_FAILURE_PASSWORD_INCORRECT))
    }
  }

}

object AskSpec {
  //assume this code is somewhere else in your app
  case class Read(key: String)
  case class Write(key: String, value: String)
  class KVActor extends Actor with ActorLogging {
    override def receive: Receive = online(Map())

    def online(kv: Map[String, String]): Receive = {
      case Read(key) =>
        log.info(s"Trying to read the value at the key $key")
        sender() ! kv.get(key) //option[string]
      case Write(key,value) =>
        log.info(s"Writing the value $value for the key $key")
        context.become(online(kv + (key -> value)))
    }
  }
  //user authenticator actor
  case class RegisterUser(username: String, password: String)
  case class Authenticate(username: String, password: String)
  case class AuthFailure(message: String)
  case object AuthSuccess

  object AuthManager{
    val AUTH_FAILURE_NOT_FOUND = "username not found"
    val AUTH_FAILURE_PASSWORD_INCORRECT = "password incorrect"
    val AUTH_FAILURE_SYSTEM = "system error"
  }
  class AuthManager extends Actor with ActorLogging {
    import AuthManager._

    //step 2 -- ? needs some implicits
    implicit val timeout: Timeout = Timeout(1 second)
    implicit val executionContext: ExecutionContext = context.dispatcher

    protected val authDb = context.actorOf(Props[KVActor])
    override def receive: Receive = {
      case RegisterUser(username, password) => authDb ! Write(username, password)
      case Authenticate(username, password) => handleAuthentication(username, password)

    }

    def handleAuthentication(username: String, password: String) = {
      val originalSender = sender()
      //step 3 ask the actor
      val future = authDb ? Read(username)
      //step 4 - handle the future
      future.onComplete {
        //step 5--Never call methods on the actor instance or access mutable state in onComplete
        // for that what we will do is assign sender() to some val. and use that val instead of sender()
        case Success(None) => originalSender ! AuthFailure(AUTH_FAILURE_NOT_FOUND)
        case Success(Some(dbPassword)) =>
          if (dbPassword == password) originalSender ! AuthSuccess
          else originalSender ! AuthFailure(AUTH_FAILURE_PASSWORD_INCORRECT)
        case Failure(_) => originalSender ! AuthFailure(AUTH_FAILURE_SYSTEM)
      }
    }
  }

  class PipedAuthManager extends AuthManager {
    import AuthManager._
    override def handleAuthentication(username: String, password: String): Unit = {
      //step 3 - ask the actor
      val future = authDb ? Read(username) //Future[Any]
      //step 4 - process the future untill you get the responses you will send back
      val passwordFuture = future.mapTo[Option[String]] //Future[Option[String]]
      val responseFuture = passwordFuture.map {
        case Some(dbPassword) =>
          if (dbPassword == password) AuthSuccess
          else AuthFailure(AUTH_FAILURE_PASSWORD_INCORRECT)
        case None => AuthFailure(AUTH_FAILURE_NOT_FOUND)
      } //Future[Any] - will be completed with response I will send back
      //and sending back the response with responseFuture is done via `pipeTo`
      //step 5 - pipe the resulting future to  the actor you want to send the result to
      responseFuture.pipeTo(sender())
      /*
      when the future completes send the response to the actor ref in the arg list. this is what pipeto does
       */
    }
  }

}
