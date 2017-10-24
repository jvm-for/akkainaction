package com.damoshow.aia.structure

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._

/**
  *
  */
class ScatterGatherTest extends TestKit(ActorSystem("ScatterGatherTest"))
  with WordSpecLike with BeforeAndAfterAll {

  val timeout = 2.seconds

  "The ScatterAndGather" must {
    "scatter the message and gather them again" in {

      val endProbe = TestProbe()
      val aggregateRef = system.actorOf(
        Props(new Aggregator(timeout, endProbe.ref))
      )
      val speedRef = system.actorOf(
        Props(new GetSpeed(aggregateRef))
      )
      val timeRef = system.actorOf(
        Props(new GetTime(aggregateRef))
      )
      val actorRef = system.actorOf(
        Props(new RecipientList(Seq(speedRef, timeRef)))
      )

      val photoDate = DateTime.now()
      val photoSpeed = 60
      val msg = PhotoMessage("id1", ImageProcessing.createPhotoString(photoDate, photoSpeed))

      actorRef ! msg

      val combineMsg = PhotoMessage(msg.id, msg.photo, Some(photoDate), Some(photoSpeed))

      endProbe.expectMsg(combineMsg)
    }
  }
}
