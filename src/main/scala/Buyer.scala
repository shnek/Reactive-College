import akka.actor.{Actor, ActorRef, ActorSystem}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


case object Start

class Buyer(money : BigInt, items: List[String]) extends Actor {

  import Message._
  var current = 1: BigInt



  val system = ActorSystem("Auctions")
  val AuctionSearch = context.actorSelection("/user/auctionSearch")

  system.scheduler.scheduleOnce(1 second){
    self ! "Start"
  }

  override def receive = {
    case "Start" => AuctionSearch ! GetAuctions(items)
    case auctions: List[ActorRef] => auctions.foreach(auction => {println(auction.path); auction ! Bid(current)})

    case Current(amount, buyer) if(buyer != self) =>
      current = amount + 1; if (current <= money) sender ! Bid(current)

    case Current(amount, buyer) => {}

    case AuctionDone(winner, amount) if(winner == self) => {
      println(self + ": I won for " + amount)
    }
  }


}

