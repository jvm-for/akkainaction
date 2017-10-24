package com.damoshow.aia.structure

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

/**
  *
  */
class RecipientListTest extends TestKit(ActorSystem("RecipientListTest"))
  with WordSpecLike with BeforeAndAfterAll {

  "The Recipient List" must {
    "send the message to next" in {
      val endProbe1 = TestProbe()
      val endProbe2 = TestProbe()
      val endProbe3 = TestProbe()

      val list = Seq(endProbe1.ref, endProbe2.ref, endProbe3.ref)

      val actorRef = system.actorOf(
        Props(new RecipientList(list))
      )

      val msg = "message"
      actorRef ! msg

      endProbe1.expectMsg(msg)
      endProbe2.expectMsg(msg)
      endProbe3.expectMsg(msg)
    }
  }

}
