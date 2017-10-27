package com.damoshow.aia.channels

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

/**
  *
  */
class SubchannelBusTest extends TestKit(ActorSystem("SubchannelBusTest"))
  with WordSpecLike with BeforeAndAfterAll {

  "SubchannelBusImpl and StartWithClassifier" must {
    "start the same string" in {
      val subchannelBus = new SubchannelBusImpl

      val testActor = TestProbe()

      subchannelBus.subscribe(testActor.ref, "abc")
      subchannelBus.publish(MsgEnvelope("xyzabc", "x"))
      subchannelBus.publish(MsgEnvelope("bcdef", "b"))
      subchannelBus.publish(MsgEnvelope("abc", "c"))
      testActor.expectMsg("c")
      subchannelBus.publish(MsgEnvelope("abcdef", "d"))
      testActor.expectMsg("d")
    }
  }


}
