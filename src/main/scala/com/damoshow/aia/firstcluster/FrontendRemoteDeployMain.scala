package com.damoshow.aia.firstcluster

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

/**
  *
  */
object FrontendRemoteDeployMain extends App with Startup {

  override val config = ConfigFactory.load("frontend-remote-deploy")
  override implicit val system = ActorSystem("frontend", config)

  val api = new RestApi() {

    override def createBoxOffice: ActorRef = system.actorOf(
      BoxOffice.props, BoxOffice.name
    )

    override implicit def requestTimeout: Timeout = Timeout(3.seconds)

  }

  startup(api.routes)
}
