package com.damoshow.aia.cluster

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props, ReceiveTimeout, SupervisorStrategy, Terminated}
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}
import akka.routing.BroadcastPool

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by liaoshifu on 17/11/8
  */
object JobMaster {

  case class StartJob(name: String, text: List[String])
  case class Enlist(worker: ActorRef)

  case object NextTask
  case class TaskResult(map: Map[String, Int])

  case object Start
  case object MergeResults

  def props = Props(new JobMaster)
}

class JobMaster extends Actor with ActorLogging with CreateWorkerRouter {
  import JobReceptionist.WordCount
  import JobMaster._
  import JobWorker._
  import context._

  var textParts: Vector[List[String]] = Vector()
  var intermediateResult: Vector[Map[String, Int]] = Vector()
  var workGiven = 0
  var workReceived = 0
  var workers: Set[ActorRef] = Set()

  val router: ActorRef = createWorkerRouter

  override def supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy

  override def receive: Receive = idle

  def idle: Receive = {
    case StartJob(jobName, text) =>
      textParts = text.grouped(10).toVector
      val cancellable = context.system.scheduler.schedule(0 millis, 1000 millis, router, Work(jobName, self))
      context.setReceiveTimeout(60 seconds)
      become(working(jobName, sender, cancellable))
  }

  def working(jobName: String, receptionist: ActorRef, cancellable: Cancellable): Receive = {
    case Enlist(worker) =>
      watch(worker)
      workers = workers + worker

    case NextTask =>
      if (textParts.isEmpty) {
        sender() ! WorkLoadDepleted
      } else {
        sender() ! Task(textParts.head, self)
        workGiven = workGiven + 1
        textParts = textParts.tail
      }

    case TaskResult(countMap) =>
      intermediateResult = intermediateResult :+ countMap
      workReceived = workReceived + 1

      if (textParts.isEmpty && workGiven == workReceived) {
        cancellable.cancel()
        become(finishing(jobName, receptionist, workers))
        setReceiveTimeout(Duration.Undefined)
        self ! MergeResults
      }

    case ReceiveTimeout =>
      if (workers.isEmpty) {
        log.info(s"No workers responsed in time, Cancellable job $jobName˜")
        stop(self)
      } else setReceiveTimeout(Duration.Undefined)

    case Terminated(worker) =>
      log.info(s"Worker $worker got terminated. Cancelling job $jobName")
      stop(self)
  }

  def finishing(jobName: String, receptionist: ActorRef, workers: Set[ActorRef]): Receive = {
    case MergeResults =>
      val mergedMap = merge()
      workers.foreach(stop)
      receptionist ! WordCount(jobName, mergedMap)
  }

  private def merge(): Map[String, Int] = {
    intermediateResult.foldLeft(Map.empty[String, Int])((el, acc) => el.map { case (word, count) => acc.get(word).map(accCount => word -> (accCount + count)).getOrElse(word -> count) } ++ (acc -- el.keys))
  }
}

trait CreateWorkerRouter {
  this: Actor =>

  def createWorkerRouter: ActorRef = {
    context.actorOf(
      ClusterRouterPool(BroadcastPool(10), ClusterRouterPoolSettings(
        totalInstances = 100, maxInstancesPerNode = 20,
        allowLocalRoutees = false
      )).props(Props[JobWorker]),
      name = "worker-router"
    )
  }
}