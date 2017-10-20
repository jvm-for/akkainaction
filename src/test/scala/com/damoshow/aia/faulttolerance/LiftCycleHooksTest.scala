package com.damoshow.aia.faulttolerance

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

/**
  * 
  */
class LiftCycleHooksTest extends TestKit(ActorSystem("testSystem"))
  with WordSpecLike with BeforeAndAfterAll {

  import LifeCycleHooks._
  
  override def afterAll(): Unit = {
    system.terminate()
  }

  "The Child" must {
    "log lifecycle hooks" in {
      val testActorRef = system.actorOf(Props[LifeCycleHooks], "LifeCycleHooks")

      watch(testActorRef)
      testActorRef ! ForceRestart
      testActorRef.tell(SampleMessage, testActor)
      expectMsg(SampleMessage)
      //system.stop(testActorRef)
      testActorRef ! PoisonPill
      expectTerminated(testActorRef)
    }
  }
}
