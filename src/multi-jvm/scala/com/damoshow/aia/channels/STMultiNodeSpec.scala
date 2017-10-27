package scala.com.damoshow.aia.channels

import org.scalatest._
import akka.testkit.ImplicitSender
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpecCallbacks}
import akka.actor._
import akka.remote.transport.ThrottlerTransportAdapter.Direction

import scala.concurrent.duration._

/**
  * 
  */
trait STMultiNodeSpec extends MultiNodeSpecCallBacks
  with WordSpecLike with MustMatchers with BeforeAndAfterAll {

  override def beforeAll() = multiNodeSpecBeforeAll()

  override def afterAll() = multiNodeSpecAfterAll()
}

object ReliableProxySampleConfig extends MultiNodeConfig {
  val client = role("Client")
  val server = role("Server")
  testTransport(true)
}

class ReliableProxySampleSpecMultiJvmNode1 extends ReliableProxySample
class ReliableProxySampleSpecMultiJvmNode2 extends ReliableProxySample

class ReliableProxySample extends MultiNodeSpec(ReliableProxySampleConfig)
  with STMultiNodeSpec with ImplicitSender {

  import ReliableProxySampleConfig._

  def initialParticipants = roles.size

  "A MultiNodeSample" must {
    "wait for all nodes to enter a barrie" in {
      enterBarrier("startup")
    }

    "send to and receive from a remote node " in {
      runOn(client) {
        enterBarrier("deployed")

        val pathToEcho = node(server) / "user" / "echo"
        val echo = system.actorSelection(pathToEcho)
        val proxy = system.actorOf(
          ReliableProxy.props(pathToEcho, 500.millis), "proxy"
        )

        proxy ! "message1"
        expectMsg("message1")

        Await.ready(
          testConductor.blackhole(client, server, Direction.Both), 1 second
        )

        echo ! "DirectMessage"
        proxy ! "ProxyMessage"
        expectNoMsg(3 seconds)

        Await.ready(
          testConductor.passThrought(client, server, Direction.Both), 1 second
        )

        expectMsg("ProxyMessage")

        echo ! "DirectMessage2"
        expectMsg("DirectMessage2")
      }

      runOn(server) {
        system.actorOf(Props(new Actor {
          def receive = {
            case msg: AnyRef =>
              sender ! msg
          }
        }), "echo")

        enterBarrier("deployed")
      }

      enterBarrier("finished")
    }
  }
}