package com.damoshow.swagger

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.RouteConcatenation
import akka.stream.ActorMaterializer
import com.damoshow.swagger.actor.{AddActor, HelloActor}
import com.damoshow.swagger.doc.SwaggerDocService
import com.damoshow.swagger.service.{AddService, HelloService}
import akka.http.scaladsl.server.Directives.{pathPrefix, getFromResourceDirectory, pathSingleSlash, get, redirect}

/**
  *
  */
object AkkaSwaggerApp extends RouteConcatenation {

  def main(args: Array[String]): Unit = {

    import spray.json.DefaultJsonProtocol._

    implicit val system = ActorSystem("Akka-Swagger")

    sys.addShutdownHook(system.terminate())

    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val hello = system.actorOf(Props[HelloActor])
    val add = system.actorOf(Props[AddActor])

    /*import akka.http.scaladsl.server.Directives._
    */
    def asserts = pathPrefix("swagger") {
      getFromResourceDirectory("swagger") ~ pathSingleSlash(get(redirect("index.html", StatusCodes.PermanentRedirect)))
    }

    val routes = asserts ~ new AddService(add).route ~ new HelloService(hello).route ~ SwaggerDocService.routes

    Http().bindAndHandle(routes, "0.0.0.0", 12345)
  }
}
