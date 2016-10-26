import akka.actor.{ActorSystem, Props}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {
  val system = ActorSystem("Auctions")

  system.actorOf(Props[AuctionSearch], "auctionSearch")

  system.actorOf(Props(classOf[Seller], List("Audi_A6", "BMW_M5")))


  val buyers = List(40,50).map(
    value => system.actorOf(Props(classOf[Buyer], BigInt(value), List("BMW")))
  )

  Await.result(system.whenTerminated, Duration.Inf)
}