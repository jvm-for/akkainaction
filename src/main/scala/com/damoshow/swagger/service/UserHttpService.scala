package com.damoshow.swagger.service

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Status}
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import akka.http.scaladsl.server._

import scala.concurrent.ExecutionContext

/**
  *
  */
/*
object UserHttpService {
  final val Name = "user-service"

  def props(address: String, port: Int, internalTimeout: Timeout, userRepository: ActorRef): Props =
    Props(new UserHttpService(address, port, internalTimeout, userRepository))

  private def route(userService: ActorRef, address: String, port: Int, internalTimeout: Timeout,
                    userRepository: ActorRef, system: ActorSystem)(implicit ec: ExecutionContext, mat: Materializer) = {
    import Directives._

    new UserService(userRepository, internalTimeout).route
  }
}

class UserHttpService(address: String, port: Int, internalTimeout: Timeout, userRepository: ActorRef) extends Actor with ActorLogging {

  import UserHttpService._
  import context.dispatcher

  private implicit val mat = ActorMaterializer()

  Http()
    .bindAndHandle(route(self, address, port, internalTimeout, userRepository, context.system), address, port)
    .pipeTo(self)

  override def receive = binding

  private def binding: Receive = {
    case Http.ServerBinding(address) =>
      log.info("Listening on {}", address)

    case Status.Failure(cause) =>
      log.error(cause, s"Can't bind to $address")
      context.stop(self)
  }

}

class UserService(userRepository: ActorRef, internalTimeout: Timeout)(implicit ec: ExecutionContext) extends Directives {
  import CirceSupport._

  import io.circe.generic.auto._
}
*/
