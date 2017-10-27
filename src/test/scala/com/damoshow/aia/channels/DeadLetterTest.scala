package com.damoshow.aia.channels

import akka.actor.{ActorSystem, DeadLetter, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.damoshow.aia.testdriven.EchoActor
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}

/**
  *
  */
class DeadLetterTest extends TestKit(ActorSystem("DeadLetterTEst"))
  with WordSpecLike with MustMatchers with BeforeAndAfterAll with ImplicitSender {
  
  "DeadLetterChannel" must {
    "catch message send to terminated Actor " in {
      val deadLetterMonitor = TestProbe()
      system.eventStream.subscribe(
        deadLetterMonitor.ref,
        classOf[DeadLetter]
      )

      val actor = system.actorOf(Props[EchoActor], "echo")
      actor ! PoisonPill
      val msg = Order("nba", "NBA in action", 2)
      actor ! msg

      val dead = deadLetterMonitor.expectMsgType[DeadLetter]
      dead.message must be (msg)
      dead.sender must be(testActor)
      dead.recipient must be(actor)
    }

    "catch deadLetter messages send to deadLetters" in {
      val deadLetterMonitor = TestProbe()
      val actor = system.actorOf(Props[EchoActor], "echo")

      system.eventStream.subscribe(
        deadLetterMonitor.ref,
        classOf[DeadLetter]
      )

      val msg = Order("nba", "NBA in action", 10)
      val dead = DeadLetter(msg, testActor, actor)
      system.deadLetters ! dead

      system.stop(actor)

      deadLetterMonitor.expectMsg(dead)

      //system.stop(actor)
    }
  }
}
