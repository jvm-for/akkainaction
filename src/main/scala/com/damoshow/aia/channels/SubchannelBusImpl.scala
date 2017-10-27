package com.damoshow.aia.channels

import akka.actor.ActorRef
import akka.event.{EventBus, SubchannelClassification}
import akka.util.Subclassification

final case class MsgEnvelope(topic: String, payload: Any)

class SubchannelBusImpl extends EventBus with SubchannelClassification {
  override type Event = MsgEnvelope
  override type Classifier = String
  override type Subscriber = ActorRef

  override protected val subclassification: Subclassification[Classifier] =
    new StartsWithSubclassification

  override protected def classify(event: MsgEnvelope): String = event.topic

  override protected def publish(event: Event, subscriber: Subscriber) = {
    subscriber ! event.payload
  }
}

class StartsWithSubclassification extends Subclassification[String] {
  override def isEqual(x: String, y: String): Boolean = x == y

  override def isSubclass(x: String, y: String): Boolean = x.startsWith(y)
}
