package com.damoshow.aia.firstcluster

import akka.actor.{ActorRef, ActorSystem, AddressFromURIString, Deploy, Props}
import akka.remote.RemoteScope
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

    /*val uri = "akka.tcp://backend@0.0.0.0:2551"
    val backendAddress = AddressFromURIString(uri)
    val props = Props(new BoxOffice()).withDeploy(
      Deploy(scope = RemoteScope(backendAddress))
    )
    override def createBoxOffice: ActorRef = system.actorOf(props, BoxOffice.name)
    */
    override def createBoxOffice: ActorRef = system.actorOf(
      BoxOffice.props, BoxOffice.name
    )


    override implicit def requestTimeout: Timeout = Timeout(3.seconds)

  }

  startup(api.routes)
}
