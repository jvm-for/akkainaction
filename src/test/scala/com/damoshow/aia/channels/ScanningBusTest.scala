package com.damoshow.aia.channels

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

/**
  * 
  */
class ScanningBusTest extends TestKit(ActorSystem("ScanningBusTest"))
  with WordSpecLike with BeforeAndAfterAll {

  "ScanningBus" must {
    "deliver message" in {
      val testActor = TestProbe()
      val scanningBus = new ScanningBusImpl

      scanningBus.subscribe(testActor.ref, 3)
      scanningBus.publish("nbaabdc")
      scanningBus.publish("dc")
      testActor.expectMsg("dc")

      scanningBus.publish("bdc")
      testActor.expectMsg("bdc")

      scanningBus.publish("jaq")
      testActor.expectMsg("jaq")

      scanningBus.publish("ade")
      testActor.expectMsg("ade")
    }
  }
}
