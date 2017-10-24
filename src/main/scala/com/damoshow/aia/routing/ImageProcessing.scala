package com.damoshow.aia.routing

import org.joda.time.DateTime

import scala.util.Try

/**
  *
  */
case class Photo(license: String, speed: Int)

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
