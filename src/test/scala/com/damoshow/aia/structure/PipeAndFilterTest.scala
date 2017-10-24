package com.damoshow.aia.structure

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._

/**
  *
  */
class PipeAndFilterTest extends TestKit(ActorSystem("PipeAndFilterTest"))
  with WordSpecLike with BeforeAndAfterAll {

  val timeout = 2 seconds

  override def afterAll(): Unit = {
    system.terminate()
  }

  "The pipe and filter " must {
    "filter message in configuration 1 " in {
      val endProbe = TestProbe()

      val speedFilterRef = system.actorOf(
        Props(new SpeedFilter(50, endProbe.ref))
      )

      val licenseFilterRef = system.actorOf(
        Props(new LicenseFilter(speedFilterRef))
      )

      val msg = new Photo("123xyz", 60)
      licenseFilterRef ! msg

      endProbe.expectMsg(msg)

      licenseFilterRef ! new Photo("", 60)
      endProbe.expectNoMessage(timeout)

      licenseFilterRef ! new Photo("abc233", 49)
      expectNoMessage(timeout)
    }

    "filter messages in configuration 2" in {

      val endProbe = TestProbe()
      val licenseFilter = system.actorOf(
        Props(new LicenseFilter(endProbe.ref))
      )

      val speedFilter = system.actorOf(
        Props(new SpeedFilter(50, licenseFilter))
      )

      val msg = Photo("223abc", 60)
      speedFilter ! msg

      endProbe.expectMsg(msg)

      speedFilter ! Photo(null, 60)
      endProbe.expectNoMessage(timeout)

      speedFilter ! Photo("abcd2332", 49)
      endProbe.expectNoMessage(timeout)
    }
  }
}
