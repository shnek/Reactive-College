import Message.GetAuctions
import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

class TestHelper extends Actor{
  import context._
  val config = ConfigFactory.load()

  val AuctionSearch = context.actorSelection("/user/masterSearch")

  var counter = 0
  var t0 = 0l

  override def receive: Receive = {
    case "start" =>
      context.actorOf(Props(classOf[Seller], List.range(0, 50000).map(item => "item" + item)))
      context.system.scheduler.scheduleOnce(1 second){
        AuctionSearch ! GetAuctions(List("item"))
      }



    case auctions: List[ActorRef] if(auctions.isEmpty) =>
      counter += 1
      if(counter.equals(10000)){
        val t1 = System.nanoTime()
        println("Elapsed time: " + (t1 - t0) + "ns")
        system.terminate()
        context.parent ! (t1 - t0)
      }

    case auctions: List[ActorRef] =>
      if(auctions.size.equals(50000)){
        self ! "startBuyer"
      } else {
        println("Currently there are " + auctions.size + " auctions")
        context.system.scheduler.scheduleOnce(1 second){
          AuctionSearch ! GetAuctions(List("item"))
        }
      }

    case "startBuyer" =>
      println("Starting test")
      t0 = System.nanoTime()
      List.range(0, 10000).foreach(_ => AuctionSearch ! GetAuctions(List("none")))
  }
}
