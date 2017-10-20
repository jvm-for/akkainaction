package com.damoshow.aia.testdriven

import akka.actor.{ActorSystem, UnhandledMessage}
import akka.testkit.TestKit
import org.scalatest.WordSpecLike

/**
  * 2017-10-17
  */
class Greeter02Test extends TestKit(ActorSystem("testSystem"))
  with WordSpecLike
  with StopSystemAfterAll {

  import Greeter02._

  "The Greeter" must {
    "say Hello World! when a Greeting(\"World\") is sent to it" in {
      val props = Greeter02.props(Some(testActor))
      val greeter = system.actorOf(props, "greeter02-1")

      greeter ! Greeting("World")

      expectMsg("Hello World!")
    }

    "say something else and see what happens" in {
      val props = Greeter02.props(Some(testActor))
      val greeter = system.actorOf(props, "greeter02-2")

      system.eventStream.subscribe(testActor, classOf[UnhandledMessage])
      greeter ! "World"
      expectMsg(UnhandledMessage("World", system.deadLetters, greeter))
    }
  }
}
