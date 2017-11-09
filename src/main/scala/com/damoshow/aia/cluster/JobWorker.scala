package com.damoshow.aia.cluster

import akka.actor.{Actor, ActorLogging, ActorRef, Props, ReceiveTimeout, Terminated}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by liaoshifu on 17/11/9
  */
object JobWorker {

  def props = Props(new JobWorker)

  case class Work(jobName: String, master: ActorRef)
  case class Task(input: List[String], master: ActorRef)
  case object WorkLoadDepleted
}

class JobWorker extends Actor with ActorLogging {

  import JobMaster._
  import JobWorker._
  import context._

  var processed = 0

  override def receive: Receive = idle

  def idle: Receive = {
    case Work(jobName, master) =>
      become(enlisted(jobName, master))

      log.info(s"Enlisted, will start requesting work for job '$jobName'.")
      master ! Enlist(self)
      master ! NextTask
      watch(master)

      setReceiveTimeout(30 seconds)
  }

  def enlisted(jobName: String, m: ActorRef): Receive = {
    case ReceiveTimeout =>
      m ! NextTask

    case Task(textPart, master) =>
      val countMap = processTask(textPart)
      processed = processed + 1
      master ! TaskResult(countMap)
      master ! NextTask

    case WorkLoadDepleted =>
      log.info(s"Work load $jobName is depleted, retiring")
      setReceiveTimeout(Duration.Undefined)
      become(retired(jobName))
  }

  def retired(jobName: String): Receive = {
    case Terminated(master) =>
      log.error(s"Master ${master.path} terminated theat ran Job $jobName, stopping self.")
      stop(self)

    case _ =>
      log.error("I'm retired.")
  }

  def processTask(textPart: List[String]): Map[String, Int] = {
    textPart.flatMap(_.split("\\W+"))
      .foldLeft(Map.empty[String, Int]) { (count, word) =>
        if (word == "FAIL") throw new RuntimeException("SIMULATED FAILURE")
        count + (word -> (count.getOrElse(word, 0) + 1))
      }
  }
}
