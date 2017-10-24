package com.damoshow.aia.structure

import akka.actor.{Actor, ActorLogging, ActorRef}
import org.joda.time.DateTime

import scala.concurrent.duration._
import scala.util.Try


case class PhotoMessage(
                       id: String,
                       photo: String,
                       creationTime: Option[DateTime] = None,
                       speed: Option[Int] = None
                       )

case class TimeoutMessage(photoMessage: PhotoMessage)

object PhotoMessage {

  def combine(p1: PhotoMessage, p2: PhotoMessage): PhotoMessage = if (p1.id == p2.id) PhotoMessage(
    p1.id, p1.photo, p1.creationTime.orElse(p2.creationTime), p1.speed.orElse(p2.speed)
  ) else p1
}

object ImageProcessing {

  def getSpeed(image: String): Option[Int] = {
    val attributes = image.split('|')
    if (attributes.size == 3)
      Try(attributes(1).toInt).toOption
    else None
  }

  def getTime(image: String): Option[DateTime] = {
    import com.github.nscala_time.time.Implicits._
    get(image, 0)(d => Try(d.toDateTime).toOption)
  }

  def getLicense(image: String): Option[String] = {
    get(image, 2)(Some(_))
  }

  def createPhotoString(dateTime: DateTime, speed: Int): String = {
    createPhotoString(dateTime, speed, " ")
  }

  def get[A](image: String, index: Int)(f: String => Option[A]) = {
    val attributes = image.split('|')
    if (attributes.size == 3)
      f(attributes(index))
    else None
  }

  def createPhotoString(time: DateTime, speed: Int, license: String): String =
    "%s|%s|%s".format(time.toString, speed, license)

}

class Aggregator(timeout: FiniteDuration, pipe: ActorRef) extends Actor with ActorLogging {

  var messages = List.empty[PhotoMessage]

  implicit val ec = context.dispatcher

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    messages.foreach(self ! _)
    messages = Nil
  }

  override def receive: Receive = {
    case msg: PhotoMessage =>
      messages.find(_.id == msg.id) match {
        case Some(alreadyRcvMsg) =>
          val combined = PhotoMessage.combine(alreadyRcvMsg, msg)
          pipe ! combined
          messages = messages.filterNot(_.id == msg.id)

        case None =>
          messages = msg :: messages
          context.system.scheduler.scheduleOnce(5.second, self, TimeoutMessage(msg))
      }

    case TimeoutMessage(msg) =>
      messages.find(_.id == msg.id) match {
        case Some(alreadyRcvMsg) =>
          pipe ! alreadyRcvMsg
          messages = messages.filterNot(_.id == msg.id)

        case None =>
          log.debug("The message: {} timeout after combine", msg)
      }

    case ex: Exception => throw ex
  }
}

class GetSpeed(aggregatorRef: ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {
    case pm: PhotoMessage =>
      log.info("GetSpeed received message: {}", pm)
      aggregatorRef ! pm.copy(speed = ImageProcessing.getSpeed(pm.photo))
  }
}

class GetTime(aggregatorRef: ActorRef) extends Actor with ActorLogging {
  override def receive: Receive = {
    case pm @ PhotoMessage(_, photo, _, _)  =>
      log.info("GetTime received message: {}", pm)
      aggregatorRef ! pm.copy(creationTime = ImageProcessing.getTime(photo))
  }
}

class RecipientList(actorRefs: Seq[ActorRef]) extends Actor with ActorLogging {

  override def receive: Receive = {
    case pm: AnyRef =>
      log.info("RecipientList received message: {}", pm)
      actorRefs.foreach(_ ! pm)
  }
}