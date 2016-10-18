import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

case object Start

class Buyer(money: BigInt, auctions: List[ActorRef]) extends Actor {

  import Message._

  var current = 1: BigInt

  override def receive = {
    case Start => {
      auctions(0) ! Bid(current)
      current += 1
    }
    case CurrentPrice(amount) => {
      current = amount + 1
      if (current < money) sender ! Bid(current)
    }
  }

}

object Main extends App {
  val system = ActorSystem("Auctions")

  val auctions : List[ActorRef] = List(3).map(
    value => system.actorOf(Props(classOf[Auction], BigInt(value)), "auction" + value)
  )

  val buyers = List(40,50).map(
    value => system.actorOf(Props(classOf[Buyer], BigInt(value), auctions), "buyer" + value)
  )

  buyers.foreach(ActorRef => ActorRef ! Start)

  Await.result(system.whenTerminated, Duration.Inf)
}