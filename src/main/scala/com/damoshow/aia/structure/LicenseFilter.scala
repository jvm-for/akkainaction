package com.damoshow.aia.structure

import akka.actor.{Actor, ActorLogging, ActorRef}
import org.apache.commons.lang3.StringUtils

/**
  *
  */
case class Photo(license: String, speed: Int)

class LicenseFilter(nextActor: ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {
    case msg @ Photo(license, _) if !StringUtils.isEmpty(license) =>
      nextActor ! msg
  }
}

class SpeedFilter(maxSpeed: Int, nextActor: ActorRef) extends Actor {

  override def receive: Receive = {
    case msg @ Photo(_, speed) if (speed > maxSpeed) =>
      nextActor ! msg
  }
}
