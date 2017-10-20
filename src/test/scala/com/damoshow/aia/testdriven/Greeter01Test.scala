package com.damoshow.aia.testdriven

import akka.actor.{ActorSystem, Props}
import akka.testkit.{CallingThreadDispatcher, EventFilter, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{MustMatchers, WordSpecLike}

import Greeter01Test._

/**
  * 2017-10-17
  */
class Greeter01Test extends TestKit(testSystem)
  with WordSpecLike
  with MustMatchers
  with StopSystemAfterAll {

  import Greeter._

  "The Greeter Actor" must {
    """say hello world! when a Greeting("world") is sent to it""" in {
      val dispatcherId = CallingThreadDispatcher.Id
      val props = Props[Greeter].withDispatcher(dispatcherId)
      val greeter = system.actorOf(props)
      EventFilter.info(message = "hello world!", occurrences = 1).intercept {
        greeter ! Greeting("world")
      }
    }
  }
}

object Greeter01Test {
  val testSystem = {
    val config = ConfigFactory.parseString(
      """
        |akka.loggers = [akka.testkit.TestEventListener]
      """.stripMargin
    )
    ActorSystem("testSystem", config)
  }
}