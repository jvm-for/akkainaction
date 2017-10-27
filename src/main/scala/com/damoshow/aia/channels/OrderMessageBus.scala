package com.damoshow.aia.channels

import akka.event.{ActorEventBus, EventBus, LookupClassification}

/**
  *
  */
class OrderMessageBus extends EventBus with LookupClassification with ActorEventBus {
  override type Event = Order
  override type Classifier = Boolean

  override def mapSize(): Int = 2

  override protected def classify(event: Order): Boolean =
    event.number > 1

  override protected def publish(event: Order, subscriber: Subscriber): Unit =
    subscriber ! event
}
