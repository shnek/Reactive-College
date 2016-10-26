import Message.{GetAuctions, RegisterAuction}
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class AuctionSearchTest extends TestKit(ActorSystem("Auctions"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate
  }

  "auction added correctly" in {
    val auctionName = "AuctionName"
    val auctionSearch = system.actorOf(Props[AuctionSearch])
    val seller = system.actorOf(Props(classOf[Seller], List()))
    val auction = system.actorOf(Props[Auction], auctionName)


    auctionSearch ! RegisterAuction(auction)
    auctionSearch ! GetAuctions(List(""))
    expectMsg(List(auction))
  }


}
