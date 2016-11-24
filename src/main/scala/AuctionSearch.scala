import Message.{GetAuctions, RegisterAuction}
import akka.actor.{Actor, ActorRef, ActorSystem}


class AuctionSearch extends Actor{
  var auctions = List[ActorRef]()

  override def receive: Receive = {
    case RegisterAuction(auctionRef) => {
      auctions ::= auctionRef
    }
    case GetAuctions(names) => {
      val filtered = auctions.filter(
        (auction) => {
          names.map(
            (n) => auction.path.name.contains(n)
          ).reduce((a,b) => a || b)
        }
      )
//      println("Response: " + filtered)
      sender ! filtered
    }
  }
}
