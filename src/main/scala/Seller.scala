
import Message.{AuctionDone, RegisterAuction}
import akka.actor.{Actor, ActorRef, Props}

class Seller(auctions: List[String]) extends Actor{

  val AuctionSearch = context.actorSelection("/user/auctionSearch")
  auctions.foreach(auction => {
    AuctionSearch ! RegisterAuction(
      context.actorOf(Props[Auction], auction)
    )
  })

  override def receive: Receive = {
    case AuctionDone(winner, amount) => println("Yay, " + winner + "bought my auction for " + amount)
    case auctions: List[ActorRef] => auctions.foreach(a => println(a.path))
  }


}
