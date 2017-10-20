package com.damoshow.aia.testdriven

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

/**
  *
  */
object Greeter02 {
  def props(listener: Option[ActorRef]) = Props(new Greeter02(listener))
  case class Greeting(msg: String)
}

class Greeter02(listener: Option[ActorRef]) extends Actor with ActorLogging {

  import Greeter02._

  override def receive: Receive = {
    case Greeting(msg) =>
      val message = s"Hello $msg!"
      log.info(message)
      listener.foreach(_ ! message)
  }
}