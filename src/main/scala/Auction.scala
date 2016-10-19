import akka.actor.{ActorRef, FSM}

import scala.concurrent.duration._

sealed trait State

case object Created extends State

case object Activated extends State

case object Ignored extends State

case object Sold extends State

sealed trait Data

case class AuctionData(buyer: ActorRef, currentPrice: BigInt) extends Data

case object Uninitialized extends Data

class Auction extends FSM[State, Data] {

  import Message._

  startWith(Created, AuctionData(self, 0))


  when(Created) {
    case Event(Bid(amount), AuctionData(buyer, currentPrice)) if amount > currentPrice => {
      goto(Activated) using AuctionData(sender, amount)
    }
    case Event(Bid(amount), AuctionData(buyer, currentPrice)) if amount <= currentPrice => {
      sender ! Current(currentPrice, buyer)
      stay
    }
    case Event(BidTimer, _) => {
      goto(Ignored)
    }
  }
  when(Activated) {
    case Event(Bid(amount), AuctionData(buyer, currentPrice)) if amount > currentPrice => {
      buyer ! Current(amount, sender)
      stay using AuctionData(sender, amount)
    }
    case Event(Bid(amount), AuctionData(buyer, currentPrice)) if amount <= currentPrice => {
      sender ! Current(currentPrice, buyer)
      stay
    }
    case Event(BidTimer, AuctionData(buyer, currentPrice)) => {
      buyer ! AuctionDone(buyer, currentPrice)
      goto(Sold)
    }
    case Event(DeleteTimer, _) => stay
  }

  when(Ignored) {
    case Event(Bid(amount), AuctionData(buyer, currentPrice)) if amount > currentPrice => {
      buyer ! Current(amount, sender)
      goto(Activated) using AuctionData(sender, amount)
    }
    case Event(Bid(amount), AuctionData(buyer, currentPrice)) => sender ! Current(currentPrice, buyer); stay
    case Event(DeleteTimer, _) => println("Auction is dead!"); stop
  }
  when(Sold) {
    case Event(DeleteTimer, _) => println("Auction is sold!"); stop
  }

  onTransition {
    case x -> Created => setTimer("BidTimer", BidTimer, 10 seconds)
    case Created -> Ignored => setTimer("DeleteTimer", DeleteTimer, 5 seconds)
    case Activated -> Sold => setTimer("DeleteTimer", DeleteTimer, 10 seconds)
    case Ignored -> Created => setTimer("BidTimer", BidTimer, 10 seconds); cancelTimer("DeleteTimer")
  }

  initialize()
}


object Message {

  //inside auction messages:

  case object BidTimer

  case object DeleteTimer

  //buyer to auction messages:

  final case class Bid(amount: BigInt) {
    require(amount > 0)
  }

  //auction to buyer messages:

  final case class Current(amount: BigInt, buyer: ActorRef) {
    require(amount > 0)
  }

  final case class AuctionDone(winner: ActorRef, amount: BigInt)

}
