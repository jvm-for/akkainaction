package com.damoshow.aia.routing

import akka.actor.{Actor, ActorRef}

import scala.concurrent.duration._

import scala.language.postfixOps

/**
  *
  */
case class PerformanceRoutingMessage(
                                    photo: String,
                                    license: Option[String],
                                    processedBy: Option[String]
                                    )

case class SetService(id: String, serviceTime: FiniteDuration)

case class PreferredSize(size: Int)

case class GetLicense(pipe: ActorRef, initialServiceTime: FiniteDuration = 0 millis) extends Actor {
  var id = self.path.name

  var serviceTimeout = initialServiceTime

  override def receive: Receive = {
    case init: SetService => {
      id = init.id
      serviceTimeout = init.serviceTime
      Thread.sleep(100)
    }

    case msg: PerformanceRoutingMessage =>
      Thread.sleep(serviceTimeout.toMillis)
      pipe ! msg.copy(
        license = ImageProcessing.getLicense(msg.photo),
        processedBy = Some(id)
      )
  }
}
