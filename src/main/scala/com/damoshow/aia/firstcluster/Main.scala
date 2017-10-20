package com.damoshow.aia.firstcluster

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.damoshow.aia.firstcluster.Main.requestTimeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  *
  */
object Main extends App with Startup {

  override def config = ConfigFactory.load()

  implicit val system = ActorSystem("FirstCluster", config)


  val api = new RestApi() {

    override implicit val requestTimeout: Timeout = Timeout(5.seconds)

    override def createBoxOffice: ActorRef = system.actorOf(
      BoxOffice.props, BoxOffice.name
    )
  }

  startup(api.routes)
}

trait Startup extends RequestTimeout {

  def config: Config

  def host = config.getString("http.host")
  def port = config.getInt("http.port")

  //val api = new RestApi(system, requestTimeout(config)).routes

  implicit val system: ActorSystem

  implicit lazy val ec = system.dispatcher

  implicit lazy val materializer = ActorMaterializer()

  def api: RestApi

  def startup(route: Route) = {
    
    val bindingFuture: Future[ServerBinding] =
      Http().bindAndHandle(route, host, port)

    val log = Logging(system.eventStream, "damoshow")
    bindingFuture.map { serverBinding =>
      log.info(s"RestApi bound to ${serverBinding.localAddress}")
    }.onFailure {
      case ex: Exception =>
        log.error(ex, "Failed to bind {}:{}", host, port)
        system.terminate()
    }
  }
}

trait RequestTimeout {
  import scala.concurrent.duration._

  def requestTimeout(config: Config): Timeout = {
    val t = config.getString("akka.http.server.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }
}
