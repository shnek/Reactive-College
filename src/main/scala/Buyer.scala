import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.concurrent.Await
import scala.concurrent.duration.Duration


case object Start

class Buyer(money : BigInt, items: List[String]) extends Actor {

  import Message._
  var current = 1: BigInt

  val system = ActorSystem("Auctions")
  val AuctionSearch = context.actorSelection("/user/auctionSearch")
  AuctionSearch ! GetAuctions(items)

  override def receive = {
    case auctions: List[ActorRef] => auctions.foreach(auction => auction ! Bid(current))

    case Current(amount, buyer) if(buyer != self) =>
      current = amount + 1; if (current <= money) sender ! Bid(current)

    case Current(amount, buyer) => {}

    case AuctionDone(winner, amount) if(winner == self) => {
      println(self + ": I won for " + amount)
    }
  }


}

object Main extends App {
  val system = ActorSystem("Auctions")

  system.actorOf(Props[AuctionSearch], "auctionSearch")

  system.actorOf(Props(classOf[Seller], List("Audi_A6", "BMW_M5")))


  val buyers = List(40,50).map(
    value => system.actorOf(Props(classOf[Buyer], BigInt(value), List("BMW")))
  )

  Await.result(system.whenTerminated, Duration.Inf)
}