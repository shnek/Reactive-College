import akka.actor.ActorRef

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


  final case class RegisterAuction(auctionRef: ActorRef)

  final case class GetAuctions(names: List[String])

  final case class Notify(title: String, buyer: String, price: BigInt)
}
