package com.damoshow.aia.stream

import java.nio.file.StandardOpenOption.{APPEND, CREATE, WRITE}

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.util.ByteString
import com.damoshow.aia.stream.LogStreamProcessor.LogParseException
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import spray.json._

/**
  *
  */
object EventFilter extends App with EventMarshalling {
  val config = ConfigFactory.load()

  val maxLine = config.getInt("log-stream-processor.max-line")

  if (args.length != 3) {
    System.err.println("Provide args: input-file output-file state")
    System.exit(1)
  }

  val inputFile = FileArg.shellExpanded(args(0))
  val outputFile = FileArg.shellExpanded(args(1))

  val filterState = args(2) match {
    case State(state) => state
    case unknown =>
      System.err.println(s"Unknown state $unknown, exiting.")
      System.exit(1)
  }

  import akka.stream.scaladsl._

  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputFile)

  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFile, Set(CREATE, WRITE, APPEND))

  /*val flow: Flow[ByteString, ByteString, NotUsed] =
    Framing.delimiter(ByteString("\n"), maxLine)
    .map(_.decodeString("UTF8"))
    .map(LogStreamProcessor.parseLineEx)
    .collect { case Some(e) => e }
    .filter(_.state == filterState)
    .map(event => ByteString(event.toJson.compactPrint))
*/

  val decider: Supervision.Decider = {
    case _: LogParseException => Supervision.Resume
    case _ => Supervision.Stop
  }

  val frame: Flow[ByteString, String, NotUsed] =
    Framing.delimiter(ByteString("\n"), maxLine)
    .map(_.decodeString("UTF8"))

  val parse: Flow[String, Event, NotUsed] =
    Flow[String].map(LogStreamProcessor.parseLineEx)
    .collect { case Some(e) => e }
    .withAttributes(ActorAttributes.supervisionStrategy(decider))

  val filter: Flow[Event, Event, NotUsed] =
    Flow[Event].filter(_.state == filterState)

  val serialize: Flow[Event, ByteString, NotUsed] =
    Flow[Event].map(event => ByteString(event.toJson.compactPrint))

  implicit val system = ActorSystem("EventFilter")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(system)
      .withSupervisionStrategy(decider)
  )

  val composeFlow: Flow[ByteString, ByteString, NotUsed] =
    frame.via(parse).via(filter).via(serialize)

  val runnableGraph: RunnableGraph[Future[IOResult]] =
    source.via(composeFlow).toMat(sink)(Keep.right)

  runnableGraph.run().foreach { result =>
    println(s"Wrote ${result.count} bytes to '$outputFile'.")
    system.terminate()
  }
}
