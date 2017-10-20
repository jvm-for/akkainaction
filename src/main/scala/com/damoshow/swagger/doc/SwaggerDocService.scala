package com.damoshow.swagger.doc

import com.damoshow.swagger.service.HelloService
import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import io.swagger.models.ExternalDocs

/**
  *
  */
object SwaggerDocService extends SwaggerHttpService {
  override val apiClasses: Set[Class[_]] = Set(classOf[HelloService])

  override val host = "localhost:12345"

  override val info = Info(version = "1.0.1")

  override val externalDocs = Some(new ExternalDocs("Core Docs", "http://api-doc.damoshow.com/docs"))

  override val unwantedDefinitions = Seq("Function1", "Function1RequestContextFutureRouteResult")
}
