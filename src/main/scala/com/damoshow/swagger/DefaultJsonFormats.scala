package com.damoshow.swagger

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpEntity, StatusCode}
import spray.json._

import scala.reflect.ClassTag

/**
  * 
  */
trait DefaultJsonFormats extends DefaultJsonProtocol with SprayJsonSupport {

  def jsonObjectFormat[A: ClassTag]: RootJsonFormat[A] = new RootJsonFormat[A] {
    val ct = implicitly[ClassTag[A]]

    override def read(json: JsValue): A = ct.runtimeClass.newInstance().asInstanceOf[A]

    override def write(obj: A): JsValue = JsObject("value" -> JsString(ct.runtimeClass.getSimpleName))
  }

  implicit object UuidJsonFormat extends RootJsonFormat[UUID] {
    override def write(obj: UUID): JsValue = JsString(obj.toString)

    override def read(json: JsValue): UUID = json match {
      case JsString(x) => UUID.fromString(x)
      case x => deserializationError(s"Expected UUID as JsString, but got $x")
    }
  }
}

case class ErrorResponseException(responseStatus: StatusCode, response: Option[HttpEntity]) extends Exception