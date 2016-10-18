import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.LoggingReceive

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.duration._


case object Start

class Buyer(money: BigInt, auctions: List[ActorRef]) extends Actor {

  import Message._
  val system = akka.actor.ActorSystem("system")
  import system.dispatcher
  val auction = auctions(0)
  var current = 1: BigInt

  override def receive = {
    case Start => {
      auction ! Bid(current)
    }
    case Current(amount, buyer) if(buyer != self) => {
      current = amount + 1
      if (current <= money) sender ! Bid(current)
    }
    case Current(amount, buyer) => {}

    case AuctionDone(winner, amount) if(winner == self) => {
      println(self + " won for " + amount)
    }
  }


}

object Main extends App {
  val system = ActorSystem("Auctions")

  val auctions : List[ActorRef] = List(3,2).map(
    value => system.actorOf(Props(classOf[Auction]), "auction" + value)
  )

  val buyers = List(40,50).map(
    value => system.actorOf(Props(classOf[Buyer], BigInt(value), auctions), "buyer" + value)
  )

  buyers.foreach(ActorRef => ActorRef ! Start)

  Await.result(system.whenTerminated, Duration.Inf)
}