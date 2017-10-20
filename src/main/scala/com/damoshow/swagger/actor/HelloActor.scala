package com.damoshow.swagger.actor

import akka.actor._

/**
  *
  */
object HelloActor {
  case object AnonymousHello
  case class Hello(name: String)
  case class Greeting(greeting: String)

}

class HelloActor extends Actor with ActorLogging {

  import HelloActor._

  override def receive: Receive = {
    case AnonymousHello =>
      sender() ! Greeting("Hello")

    case Hello(name) =>
      sender() ! Greeting(s"Hello, $name")
  }
}
