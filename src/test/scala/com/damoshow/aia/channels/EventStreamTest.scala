package com.damoshow.aia.channels

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._

/**
  *
  */

class EventStreamTest extends TestKit(ActorSystem("EventStreamTest"))
  with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "EventStream" must {
    "distribute messages " in {
      val deliverOrder = TestProbe()
      val giftModule = TestProbe()

       system.eventStream.subscribe(
         giftModule.ref,
         classOf[Order]
       )

      system.eventStream.subscribe(
        deliverOrder.ref,
        classOf[Order]
      )

      val msg = Order("me", "Akka in action", 3)
      system.eventStream.publish(msg)

      giftModule.expectMsg(msg)
      deliverOrder.expectMsg(msg)
    }

    "unscribe message" in {
      val deliverOrder = TestProbe()
      val giftModule = TestProbe()

      system.eventStream.subscribe(
        giftModule.ref,
        classOf[Order]
      )

      system.eventStream.subscribe(
        deliverOrder.ref,
        classOf[Order]
      )

      val msg = Order("me", "Akka in action", 3)
      system.eventStream.publish(msg)

      giftModule.expectMsg(msg)
      deliverOrder.expectMsg(msg)

      system.eventStream.unsubscribe(giftModule.ref)
      system.eventStream.publish(msg)
      deliverOrder.expectMsg(msg)
      giftModule.expectNoMessage(1.second)
    }
  }
}
