package com.damoshow.aia.testdriven

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit}
import org.scalatest.{MustMatchers, WordSpecLike}

/**
  * 2017-10-16
  */
class SilentActor01Test extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike
  with MustMatchers
  with StopSystemAfterAll {

  "A Silent Actor" must {
    "change state when it receives a message, single threaded" in {
      // Write the test
      import SilentActor._

      val message = "whisper"
      val silentActor = TestActorRef[SilentActor]
      silentActor ! SilentMessage(message)
      silentActor.underlyingActor.state must (contain(message))
    }

    "change state when it receives a message, multi-threaded " in {
       //fail("not implemented yet")
      import SilentActor._

      val silentActor = system.actorOf(Props[SilentActor], "s3")
      silentActor ! SilentMessage("whisper1")
      silentActor ! SilentMessage("whisper2")
      silentActor ! GetState(testActor)

      expectMsg(Vector("whisper1", "whisper2"))
    }
  }
}
