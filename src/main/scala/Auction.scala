import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive

import scala.concurrent.duration._

class Auction(minPrice: BigInt) extends Actor{
  import Message._
  val system = akka.actor.ActorSystem("system")
  import system.dispatcher
  var currentPrice = 0 : BigInt
  var buyer = self

  system.scheduler.scheduleOnce(10 second){
    self ! BidTimer
  }

  def Created : Receive = LoggingReceive {
    case Bid(amount) if amount > currentPrice => {
      currentPrice = amount
      buyer = sender
      sender ! Bidded
      context become Activated
    }
    case Bid(amount) => sender ! CurrentPrice(currentPrice)
    case BidTimer => {
      system.scheduler.scheduleOnce(10 second){
        self ! DeleteTimer
      }
      context become Ignored
    }
  }

  def Activated : Receive = LoggingReceive {
    case Bid(amount) if amount > currentPrice => {
      currentPrice = amount
      buyer = sender
      sender ! Bidded
    }
    case Bid(amount) => sender ! CurrentPrice(currentPrice)
    case BidTimer => {

      system.scheduler.scheduleOnce(10 second){
        buyer ! AuctionDone(buyer, currentPrice)
        self ! DeleteTimer
      }
      context become Sold
    }
  }

  def Ignored : Receive = LoggingReceive {
    case Relist => {
      system.scheduler.scheduleOnce(10 second){
        self ! DeleteTimer
      }
      context become Created
    }
    case DeleteTimer => {
      context.system.terminate
    }
  }

  def Sold : Receive = LoggingReceive {
    case DeleteTimer => {
      context.system.terminate
    }
  }

  override def receive = Created
}


object Message {
  //inside auction messages:
  case object Bidded
  case object Relist
  case object BidTimer
  case object DeleteTimer

  //buyer to auction messages:
  case class Bid(amount: BigInt){
    require(amount > 0)
  }
  //auction to buyer messages:
  case class CurrentPrice(amount: BigInt){
    require(amount > 0)
  }

  case class AuctionDone(winner: ActorRef, amount: BigInt)

}
