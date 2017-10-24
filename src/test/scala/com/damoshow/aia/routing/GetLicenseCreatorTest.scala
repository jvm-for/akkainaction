package com.damoshow.aia.routing

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.routing.{Broadcast, RoundRobinGroup}
import akka.testkit.{TestKit, TestProbe}
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  *
  */
class GetLicenseCreatorTest extends TestKit(ActorSystem("GetLicenseCreatorTest"))
  with WordSpecLike with BeforeAndAfterAll {

  "The routerGroup" must {
    "Use recreated routees" in {
      val endProbe = TestProbe()
      val creator = system.actorOf(
        Props(new GetLicenseCreator2(2, endProbe.ref)), "Creator"
      )

      val paths = List(
        "/user/Creator/GetLicense0",
        "/user/Creator/GetLicense1"
      )
      val router = system.actorOf(
        RoundRobinGroup(paths).props(), "groupRouter"
      )

      router ! Broadcast(PoisonPill)

      Thread.sleep(100)

      val msg = PerformanceRoutingMessage(
        ImageProcessing.createPhotoString(DateTime.now, 60, "123xyz"),
        None,
        None
      )

      // test if the routees respond
      router ! msg
      endProbe.expectMsgType[PerformanceRoutingMessage](1 second)
    }
  }

}
