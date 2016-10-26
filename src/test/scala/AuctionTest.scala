import Message.Bid
import akka.actor._
import akka.testkit.{ImplicitSender, TestActorRef, TestFSMRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class AuctionTest extends TestKit(ActorSystem("Auctions"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate
  }

  "bidding an auction works" in {
    val seller = system.actorOf(Props(classOf[Seller], List()))
    val fsm = TestFSMRef(new Auction())

    val mustBeTypedProperly: TestActorRef[Auction] = fsm

    assert(fsm.stateName == Created)
    fsm ! Bid(BigInt(1))
    assert(fsm.stateName == Activated)
  }

}
