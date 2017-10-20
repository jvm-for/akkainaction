package com.damoshow.aia.testdriven

import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.concurrent.duration._

/**
  * 2017-10-16
  * 测试套件自动停止ActorSystem
  */
trait StopSystemAfterAll extends BeforeAndAfterAll {
  this: TestKit with Suite =>

  implicit val timeout = 5 seconds

  override protected def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }
}
