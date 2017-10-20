package com.damoshow.aia.firstcluster

import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorRef, ActorSystem, Identify, Props, ReceiveTimeout, Terminated}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

/**
  *
  */
object FrontendMain extends App
  with Startup {

  override val config = ConfigFactory.load("frontend")
  override implicit val system = ActorSystem("frontend", config)

  val api = new RestApi() {
    override implicit val requestTimeout: Timeout = Timeout(5.seconds)
    def createPath(): String = {
      val config = ConfigFactory.load("frontend").getConfig("backend")
      val host = config.getString("host")
      val port = config.getInt("port")
      val protocol = config.getString("protocol")
      val systemName = config.getString("system")
      val actorName = config.getString("actor")
      s"$protocol://$systemName@$host:$port/$actorName"
    }

    def createBoxOffice: ActorRef = {
      val path = createPath()
      println(s"<----------- The BoxOffice path: $path --------------->")
      system.actorOf(Props(new RemoteLookupProxy(path)), "lookupBoxOffice")
    }

  }


  startup(api.routes)
}

class RemoteLookupProxy(path: String) extends Actor with ActorLogging {
  /**
    * Sends a RecieveTimeout message if no message has been received for 3 seconds
    */
  context.setReceiveTimeout(3 seconds)

  /**
    * Immediately starts to request identity of the actor
    */
  sendIdentifyRequest()

  def sendIdentifyRequest(): Unit = {
    val selection = context.actorSelection(path)
    selection ! Identify(path)
  }

  override def receive: Receive = identify

  def identify: Receive = {
    case ActorIdentity(`path`, Some(actor)) =>

      /**
        * No longer sendds a ReceiveTimeout if actor gets no messages, since it is now active
        */
      context.setReceiveTimeout(Duration.Undefined)
      log.info("switching to active state")
      context.become(active(actor))
      /**
        * Watches remote actor for termination
        */
      context.watch(actor)

    case ActorIdentity(`path`, None) =>
      log.error(s"Remote actor with path $path is not available.")

    /**
      * Keeps trying to identify remote actor if no message is receivedd
      */
    case ReceiveTimeout =>
      sendIdentifyRequest()

    case msg: Any =>
      log.error(s"Ignoring message $msg , not ready yet.")

  }

  def active(actor: ActorRef): Receive = {
    case Terminated(actorRef) =>
      log.info(s"Actor $actorRef terminated.")
      context.become(identify)
      log.info("switching to identify state")
      context.setReceiveTimeout(3.seconds)
      sendIdentifyRequest()

    case msg =>
      actor forward msg
  }
}