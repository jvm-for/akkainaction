package com.damoshow.aia.routing

import akka.actor.{Actor, ActorRef, PoisonPill, Props, Terminated}
import akka.routing._

/**
  *
  */
class DynamicRouteeSizer(
                        nrActors: Int,
                        props: Props,
                        router: ActorRef
                        ) extends Actor {

  var nrChildren = nrActors
  var childInstanceNr = 0

  override def preStart(): Unit = {
    super.preStart()
    (0 until nrChildren).map(nr => createRoutee())
  }

  def createRoutee(): Unit = {
    childInstanceNr += 1
    val child = context.actorOf(props, "routee" + childInstanceNr)
    val selection = context.actorSelection(child.path)
    router ! AddRoutee(ActorSelectionRoutee(selection))
    context.watch(child)
  }

  override def receive: Receive = {
    case PreferredSize(size) =>
      if (size < nrChildren) {
        // remove
        context.children.take(nrChildren - size).foreach(ref => {
          val selection = context.actorSelection(ref.path)
          router ! RemoveRoutee(ActorSelectionRoutee(selection))
        })
        router ! GetRoutees
      } else {
        (nrChildren until size).map(nr => createRoutee())
      }

      nrChildren = size

    case routees: Routees => {
      // translate Routees into a actorPath
      import collection.JavaConverters._
      var active = routees.getRoutees.asScala.map {
        case x: ActorRefRoutee => x.ref.path.toString
        case x: ActorSelectionRoutee => x.selection.pathString
      }
      // process ths routee list
      for(routee <- context.children) {
        val index = active.indexOf(routee.path.toStringWithoutAddress)
        if (index >= 0) {
          active.remove(index)
        } else {
          // Child isn't used anymore by router
          routee ! PoisonPill
        }
      }

      // active contains the terminated routees
      for (terminated <- active) {
        val name = terminated.substring(terminated.lastIndexOf("/") + 1)
        val child = context.actorOf(props, name)
        context.watch(child)
      }
    }

    case Terminated(child) => {
      println("Terminated" + child)
      val selection = context.actorSelection(child.path)
      router ! RemoveRoutee(ActorSelectionRoutee(selection))
      router ! GetRoutees
    }


  }

  override def postRestart(reason: Throwable): Unit = {
    super.postRestart(reason)
    println("restart %s".format(self.path.toString))
  }
}
