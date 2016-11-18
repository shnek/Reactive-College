import akka.actor.{Actor, ActorRef}




class Buyer(money : BigInt, items: List[String]) extends Actor {

  import Message._
  var current = 1: BigInt

  val AuctionSearch = context.actorSelection("/user/masterSearch")

  override def receive = {
    case "Start" => println("actor starts bidding"); AuctionSearch ! GetAuctions(items)

    case auctions: List[ActorRef] => auctions.foreach(auction => {println(auction.path); auction ! Bid(current)})

    case Current(amount, buyer) if(buyer != self) =>
      current = amount + 1; if (current <= money) sender ! Bid(current)

    case Current(amount, buyer) => {}

    case AuctionDone(winner, amount) if(winner == self) => {
      println(self + ": I won for " + amount)
    }
  }


}

