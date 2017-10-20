package com.damoshow.swagger.actor

import akka.actor.{Actor, ActorLogging}

/**
  *
  */
object AddActor {
  case class AddRequest(numbers: Array[Int])
  case class AddResponse(sum: Int)
}

class AddActor extends Actor with ActorLogging {
  import AddActor._

  override def receive: Receive = {
    case request: AddRequest =>
      sender() ! AddResponse(request.numbers.sum)
  }
}
