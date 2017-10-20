package com.damoshow.aia.testdriven

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.concurrent.Await
import scala.util.{Failure, Success}

/**
  *
  */
class EchoActorTest extends TestKit(ActorSystem("testSystem"))
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with StopSystemAfterAll {
  
  "An Echo Actor" must  {
    "Reply with the same message it receives without ask" in {
      val echo = system.actorOf(Props[EchoActor], "echo2")
      echo ! "some message"
      expectMsg("some message")
    }

    "Reply with the same message it receives " in {
      import akka.pattern._
      import scala.concurrent.duration._

      implicit val to1 = Timeout(3.seconds)
      implicit val ec = system.dispatcher

      val echo = system.actorOf(Props[EchoActor], "echo-1")
      val future = echo.ask("some message").mapTo[String]

      future onComplete {
        case Failure(ex) =>
          println(ex.getMessage)
        case Success(s) =>
          println(s)
      }

      val r = Await.result(future, timeout)

      r must be ("some message")
    }
  }

}
