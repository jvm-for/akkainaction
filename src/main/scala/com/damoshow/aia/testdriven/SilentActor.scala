package com.damoshow.aia.testdriven

import akka.actor.{Actor, ActorRef}

/**
  * 2017-10-16
  */
object SilentActor {
  case class SilentMessage(data: String)
  case class GetState(receiver: ActorRef)
}

class SilentActor extends Actor {

  import SilentActor._

  var internalState = Vector[String]()

  override def receive = {
    case SilentMessage(data) =>
      internalState = internalState :+ data

    case GetState(receiver) =>
      receiver ! internalState
  }

  def state = internalState
}
