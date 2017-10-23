package com.damoshow.aia.firstcluster

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

/**
  * Created by liaoshifu on 2017/10/20
  */
object BackendRemoteMain extends App {

  val config = ConfigFactory.load("backend")
  implicit val system = ActorSystem("backend", config)
}
