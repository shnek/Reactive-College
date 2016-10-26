import Message.{Bid, Current}
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._

class SellerTest extends TestKit(ActorSystem("Auctions"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate
  }

  "bid an auction that you are not winning" in {
    val actor1 = system.actorOf(Props(classOf[Buyer], BigInt(10), List()))
    val actor2 = system.actorOf(Props(classOf[Buyer], BigInt(10), List()))
    val amount = 5
    actor1 ! Current(BigInt(amount), actor2)
    expectMsg(5000 millis, Bid(BigInt(amount+1)))
  }


}
