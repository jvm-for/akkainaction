package com.damoshow.aia.channels

import akka.actor.ActorRef
import akka.event.{EventBus, ScanningClassification}

/**
  *
  */
class ScanningBusImpl extends EventBus with ScanningClassification {
  override type Event = String
  override type Classifier = Int
  override type Subscriber = ActorRef

  override protected def compareClassifiers(a: Int, b: Int): Int =
    if (a < b) -1 else if (a == b) 0 else 1

  override protected def compareSubscribers(a: ActorRef, b: ActorRef): Int = a.compareTo(b)

  override protected def matches(classifier: Int, event: String): Boolean =
    event.length <= classifier

  override protected def publish(event: Event, subscriber: Subscriber): Unit = {
    subscriber ! event
  }
}
