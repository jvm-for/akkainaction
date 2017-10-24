package com.damoshow.aia.structure

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.testkit.{TestKit, TestProbe}
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._

class AggregatorTest extends TestKit(ActorSystem("AggregatorTest"))
  with WordSpecLike with BeforeAndAfterAll {

  val timeout = 2.seconds
  override def afterAll(): Unit = {
    system.terminate()
  }

  "The Aggregator" must {
    "aggregate two message" in {

      val endProbe = TestProbe()
      val actorRef = system.actorOf(
        Props(new Aggregator(1 second, endProbe.ref))
      )

      val photoStr = ImageProcessing.createPhotoString(DateTime.now, 60)
      val msg1 = PhotoMessage("id1", photoStr, Some(DateTime.now), None)

      actorRef ! msg1

      val msg2 = PhotoMessage("id1", photoStr, None, Some(60))

      actorRef ! msg2

      val combinedMsg = PhotoMessage("id1", photoStr, msg1.creationTime, msg2.speed)

      endProbe.expectMsg(combinedMsg)
    }

    "send message after timeout" in {
      val endProbe = TestProbe()
      val actorRef = system.actorOf(
        Props(new Aggregator(timeout, endProbe.ref))
      )

      val photoStr = ImageProcessing.createPhotoString(DateTime.now, 60)
      val msg1 = PhotoMessage("id1", photoStr, Some(DateTime.now), None)

      actorRef ! msg1

      endProbe.expectMsg(msg1)
    }

    "aggregate two messages when restarting" in {
      val endProbe = TestProbe()
      val actorRef = system.actorOf(
        Props(new Aggregator(timeout, endProbe.ref))
      )
      val photoStr = ImageProcessing.createPhotoString(DateTime.now, 60)

      val msg1 = PhotoMessage("id2", photoStr, Some(DateTime.now), None)
      actorRef ! msg1

      actorRef ! new IllegalStateException("restart")
      
      val msg2 = PhotoMessage("id2", photoStr, None, Some(6))
      
      actorRef ! msg2

      val combineMsg = PhotoMessage("id2", photoStr, msg1.creationTime, msg2.speed)

      endProbe.expectMsg(combineMsg)
    }
  }
}
