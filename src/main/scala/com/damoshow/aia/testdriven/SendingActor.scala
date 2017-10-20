package com.damoshow.aia.testdriven

import akka.actor.{Actor, ActorRef, Props}

/**
  * 2017-10-17
  */
object SendingActor {

  def props(receiver: ActorRef): Props = Props(new SendingActor(receiver))

  case class Event(id: Long)
  case class SortEvents(unsorted: Vector[Event])
  case class SortedEvents(sorted: Vector[Event])
}

class SendingActor(receiver: ActorRef) extends Actor {
  import SendingActor._

  override def receive: Receive = {
    case SortEvents(unsorted) =>
      receiver ! SortedEvents(unsorted.sortBy(_.id))

  }
}
