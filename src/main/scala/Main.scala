import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {
  val config = ConfigFactory.load()


  val serversystem = ActorSystem("Auctions", config.getConfig("serverapp").withFallback(config))

  serversystem.actorOf(Props[AuctionSearch], "auctionSearch")

  serversystem.actorOf(Props(classOf[Seller], List("Audi_A6", "BMW_M5")))


  val buyers = List(40,50).map(
    value => serversystem.actorOf(Props(classOf[Buyer], BigInt(value), List("BMW")))
  )
  import serversystem.dispatcher
  buyers.foreach(buyer =>
    serversystem.scheduler.scheduleOnce(1 second, buyer, "Start")
  )
  Await.result(serversystem.whenTerminated, Duration.Inf)
}