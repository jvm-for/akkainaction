package com.damoshow.aia.cluster

import akka.actor.{Actor, ActorLogging}
import akka.cluster.{Cluster, MemberStatus}
import akka.cluster.ClusterEvent._

/**
  * Created by liaoshifu on 17/11/8
  */
class ClusterDomainEventListener extends Actor with ActorLogging {
  Cluster(context.system).subscribe(self, classOf[ClusterDomainEvent])

  override def receive: Receive = {
    case MemberUp(member) =>
      log.info(s"$member UP.")

    case MemberExited(member) =>
      log.info(s"$member EXITED")

    case MemberRemoved(member, previousStatus) =>
      if (previousStatus == MemberStatus.Exiting) {
        log.info(s"Member $member gracefully exited, REMOVED")
      } else {
        log.info(s"$member downed after unreachable, REMOVED")
      }

    case UnreachableMember(m) =>
      log.info(s"$m UNREACHABLE")

    case ReachableMember(m) =>
      log.info(s"$m REACHABLE")

    case s: CurrentClusterState =>
      log.info(s"cluster state: $s")
  }

  override def postStop(): Unit = {
    Cluster(context.system).unsubscribe(self)
    super.postStop()
  }
}
