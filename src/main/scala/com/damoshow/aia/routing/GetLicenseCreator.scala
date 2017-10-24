package com.damoshow.aia.routing

import akka.actor.{Actor, ActorRef, Props, Terminated}

/**
  *
  */
class GetLicenseCreator2(nrActors: Int, nextStep: ActorRef) extends Actor {

  override def preStart(): Unit = {
    super.preStart()
    (0 until nrActors).map { nr =>
      val child = context.actorOf(Props(new GetLicense(nextStep)), "GetLicense" + nr)
      context.watch(child)
    }
  }
  
  override def receive: Receive = {
    case Terminated(child) =>
      val newChild = context.actorOf(
        Props(new GetLicense(nextStep)), child.path.name
      )
      context.watch(newChild)
  }
}
