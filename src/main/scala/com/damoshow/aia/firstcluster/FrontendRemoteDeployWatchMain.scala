package com.damoshow.aia.firstcluster

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

/**
  * Created by liaoshifu on 2017/10/21
  */
object FrontendRemoteDeployWatchMain extends App with Startup {

  val config = ConfigFactory.load("frontend-remote-deploy")
  implicit val system = ActorSystem("frontend", config)

  val api = new RestApi() {
    val log = Logging(system.eventStream, "frontend-remote-watch")
    override def createBoxOffice: ActorRef = {
      val ref = system.actorOf(RemoteBoxOfficeForwarder.props, RemoteBoxOfficeForwarder.name)
      println(s"The BoxOffice path: ${ref.path}")
      ref
    }

    override implicit val requestTimeout: Timeout = Timeout(3.seconds)
  }

  startup(api.routes)

}
