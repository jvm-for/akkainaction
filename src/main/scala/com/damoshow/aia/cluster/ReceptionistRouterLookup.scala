package com.damoshow.aia.cluster

import akka.actor.Actor
import akka.cluster.routing.{ClusterRouterGroup, ClusterRouterGroupSettings}
import akka.routing.BroadcastGroup

/**
  * Created by liaoshifu on 17/11/9
  */
trait ReceptionistRouterLookup {
  this: Actor =>

  def receptionistRouter = context.actorOf(
    ClusterRouterGroup(
      BroadcastGroup(Nil),
      ClusterRouterGroupSettings(
        totalInstances = 100,
        routeesPaths = List("/user/receptionist"),
        allowLocalRoutees = true,
        useRoles = "master"
      )
    ).props(),
    name = "receptionist-router"
  )
}
