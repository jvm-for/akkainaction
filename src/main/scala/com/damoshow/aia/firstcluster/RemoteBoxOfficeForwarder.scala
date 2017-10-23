package com.damoshow.aia.firstcluster

import akka.actor.{Actor, ActorLogging, ActorRef, Props, ReceiveTimeout, Terminated}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by liaoshifu on 2017/10/21
  */
object RemoteBoxOfficeForwarder {
  def props(implicit timeout: Timeout) =
    Props(new RemoteBoxOfficeForwarder())

  val name = "forwarder"
}

class RemoteBoxOfficeForwarder(implicit timeout: Timeout) extends Actor with ActorLogging {
    implicit val ec = context.dispatcher

  def deployAndWatch(): Unit = {
    //val config = ConfigFactory.load("frontend-remote-deploy")
    /*val path = "akka.tcp://backend@0.0.0.0:2551/remote/akka.tcp/frontend@0.0.0.0:2552/user/forwarder/boxOffice"
    val actorSelection = context.actorSelection(path)
    actorSelection.resolveOne.onComplete {
      case Success(actor) =>
        log.info(s"the BoxOffice path: ${actor.path}")
        actor
      case Failure(ex) =>
        log.warning(s"the path: $path actor is not exists, create it")

        val actor = context.actorOf(BoxOffice.props, BoxOffice.name)
        log.info(s"the BoxOffice path: ${actor.path}")
        context.watch(actor)
        log.info("switching to maybe active state")
        context.become(maybeActive(actor))
        context.setReceiveTimeout(Duration.Undefined)
    }*/
    //val actor = Await.result(context.actorSelection("user/forwarder/boxOffice").resolveOne(), timeout.duration)
    val actor = context.actorOf(BoxOffice.props, BoxOffice.name)
    log.info(s"the BoxOffice path: ${actor.path}")
    context.watch(actor)
    log.info("switching to maybe active state")
    context.become(maybeActive(actor))
    context.setReceiveTimeout(Duration.Undefined)


  }

  def maybeActive(actor: ActorRef): Receive = {
    case Terminated(actorRef) =>
      log.info(s"Actor: $actorRef terminated.")
      log.info("switching to deploying state")
      context.become(deploying)
      context.setReceiveTimeout(3.seconds)
      deployAndWatch()

    case msg =>
      actor forward msg
  }

  def deploying: Receive = {
    case ReceiveTimeout =>
      deployAndWatch()

    case msg =>
      log.error(s"Ignoring message $msg, remote actor is not ready yet.")
  }

  override def receive: Receive = deploying

  deployAndWatch()
}
