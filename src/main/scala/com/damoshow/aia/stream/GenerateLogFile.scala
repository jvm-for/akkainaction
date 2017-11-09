package com.damoshow.aia.stream

import akka.stream.scaladsl.{FileIO, Keep, RunnableGraph, Source}
import java.nio.file._
import StandardOpenOption._
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, IOResult}
import akka.util.ByteString

import scala.concurrent.Future

/**
  *
  */
object GenerateLogFile {

  def main(args: Array[String]): Unit = {
    val filePath = args(0)
    val numberOfLines = args(1).toInt
    val rnd = new java.util.Random()
    val sink = FileIO.toPath(FileArg.shellExpanded(filePath), Set(CREATE, WRITE, APPEND))

    def line(i: Int) = {
      val host = "my-host"
      val service = "my-service"
      val time = ZonedDateTime.now.format(DateTimeFormatter.ISO_INSTANT)
      val state = if (i % 10 == 0) "warning"
      else if (i % 101 == 0) "error"
      else if (i % 102 == 0) "critical"
      else "ok"

      val description = "Some description of what has happened."
      val tag = "tag"
      val metric = rnd.nextDouble() * 100
      s"$host | $service | $state | $time | $description | $tag | $metric \n"
    }

    val graph = Source.fromIterator { () =>
      Iterator.tabulate(numberOfLines)(line)
    }.map(l => ByteString(l)).toMat(sink)(Keep.right)

    implicit val system = ActorSystem("GenerateLogFile")
    implicit val ec = system.dispatcher
    implicit val materializer = ActorMaterializer()

    graph.run().foreach { result =>
      println(s"Wrote ${result.count} bytes to '$filePath'.")
      system.terminate()
    }

  }
}
