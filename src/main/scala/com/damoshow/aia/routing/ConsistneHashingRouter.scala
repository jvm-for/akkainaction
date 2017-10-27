package com.damoshow.aia.routing

import akka.actor.{Actor, ActorRef}
import akka.routing.ConsistentHashingRouter.ConsistentHashMapping

trait GatherMessage {
  val id: String
  val values: Seq[String]
}

case class GatherMessageNormal(id: String, values: Seq[String]) extends GatherMessage

class SimpleGather(nextStep: ActorRef) extends Actor {
  var messages = Map.empty[String, GatherMessage]

  override def receive: Receive = {
    case msg: GatherMessage =>
      messages.get(msg.id) match {
        case Some(previous) =>
          nextStep ! GatherMessageNormal(msg.id, previous.values ++ msg.values)
          messages -= msg.id

        case None =>
          messages += (msg.id -> msg)
      }
  }

  def hashMapping: ConsistentHashMapping = {
    case msg: GatherMessage =>
      msg.id
  }
}
