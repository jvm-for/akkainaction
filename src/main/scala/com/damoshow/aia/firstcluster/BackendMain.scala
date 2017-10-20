package com.damoshow.aia.firstcluster

import akka.actor.ActorSystem
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

/**
  *
  */
object BackendMain extends App {

  val config = ConfigFactory.load("backend")
  implicit val system = ActorSystem("backend", config)

  implicit val timeout: Timeout = Timeout(3.seconds)

  system.actorOf(BoxOffice.props, BoxOffice.name)
}
