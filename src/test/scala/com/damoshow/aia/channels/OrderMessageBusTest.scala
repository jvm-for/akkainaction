package com.damoshow.aia.channels

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps
/**
  *
  */
class OrderMessageBusTest extends TestKit(ActorSystem("OrderMessageBusTest"))
  with WordSpecLike with BeforeAndAfterAll {

  "OrderMessage " must {
    "deliver Order messages" in {
      val bus = new OrderMessageBus

      val singleBooks = TestProbe()
      bus.subscribe(singleBooks.ref, false)
      val multiBooks = TestProbe()
      bus.subscribe(multiBooks.ref, true)

      val msg = Order("me", "Akka in action", 3)
      bus.publish(msg)
      singleBooks.expectNoMessage(1.second)
      multiBooks.expectMsg(msg)

      val msg2 = Order("nba", "NBA in action", 1)
      bus.publish(msg2)
      singleBooks.expectMsg(msg2)
      multiBooks.expectNoMessage(2 seconds)
    }

    "deliver order message when multiple subscriber" in {
      val bus = new OrderMessageBus
      val listener = TestProbe()
      bus.subscribe(listener.ref, true)
      bus.subscribe(listener.ref, false)

      val msg = Order("me", "Akka in Action", 1)
      bus.publish(msg)
      listener.expectMsg(msg)

      val msg2 = Order("me", "Akka in Action", 3)
      bus.publish(msg2)
      listener.expectMsg(msg2)
    }
  }
}
