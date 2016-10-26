import Message.{AuctionDone, Bid, Current}
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._

class SellerAuctionTest extends TestKit(ActorSystem("Auctions"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate
  }

  "seller will get a message when auction is done" in {
    val parent = TestProbe()
    val amount = 10: BigInt

    val child = parent.childActorOf(Props[Auction])
    val buyer = system.actorOf(Props(classOf[Buyer], BigInt(10), List()))

    child.tell(Bid(amount), buyer)

    parent.expectMsg(25 seconds, AuctionDone(buyer, amount))
  }

}
