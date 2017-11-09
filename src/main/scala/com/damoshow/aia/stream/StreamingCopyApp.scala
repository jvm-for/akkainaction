package com.damoshow.aia.stream

import java.nio.file.{Path, Paths}
import java.nio.file.StandardOpenOption._

import akka.Done
import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString
import com.typesafe.config.ConfigFactory

import scala.concurrent._

/**
  *
  */
object StreamingCopyApp {

  def main(args: Array[String]): Unit = {

    val config = ConfigFactory.load()
    val maxLine = config.getInt("log-stream-processor.max-line")

    if (args.length != 2) {
      System.err.println("Provide args: input-file output-life")
      System.exit(1)
    }
    
    val inputFile = FileArg.shellExpanded(args(0))
    val outputFile = FileArg.shellExpanded(args(1))
    
    val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputFile)

    val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFile, Set(CREATE, WRITE, APPEND))

    val runnableGraph: RunnableGraph[Future[IOResult]] = source.to(sink)

    implicit val system = ActorSystem("StreamingCopy")
    implicit val materializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher

    runnableGraph.run().foreach { result =>
      println(s"${result.status}, ${result.count} bytes read.")
      system.terminate()
    }

    val graphLeft: RunnableGraph[Future[IOResult]] = source.toMat(sink)(Keep.left)
    val graphRight: RunnableGraph[Future[IOResult]] = source.toMat(sink)(Keep.right)
    val graphBoth: RunnableGraph[(Future[IOResult], Future[IOResult])] = source.toMat(sink)(Keep.both)
    val graphCustom: RunnableGraph[Future[Done]] = source.toMat(sink) { (l, r) =>
      Future.sequence(List(l ,r)).map(_ => Done)
    }
  }
}
