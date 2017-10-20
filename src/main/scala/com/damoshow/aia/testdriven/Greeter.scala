package com.damoshow.aia.testdriven

import akka.actor.{Actor, ActorLogging}

/**
  * 2017-10-17
  */
object Greeter {
  case class Greeting(msg: String)
}
class Greeter extends Actor with ActorLogging{

  import Greeter._

  override def receive: Receive = {
    case Greeting(msg) =>
      //log.info("hello {}!", msg)
      log.info(s"hello $msg!")
  }
}
