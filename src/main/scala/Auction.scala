import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive

import scala.concurrent.duration._

class Auction extends Actor{
  import Message._
  val system = akka.actor.ActorSystem("system")
  import system.dispatcher
  var currentPrice = 0 : BigInt
  var buyer = self

  system.scheduler.scheduleOnce(10 seconds){
    self ! BidTimer
  }

  def Created : Receive =  {
    case Bid(amount) if amount > currentPrice => {
      currentPrice = amount
      buyer = sender
      context become Activated
    }
    case Bid(amount) => sender ! Current(currentPrice, buyer)
    case BidTimer => {
      system.scheduler.scheduleOnce(5 seconds){
        self ! DeleteTimer
      }
      context become Ignored
    }
  }

  def Activated : Receive =  {
    case Bid(amount) if amount > currentPrice => {
      currentPrice = amount
      buyer ! Current(amount, sender)
      buyer = sender
    }
    case Bid(amount) => sender ! Current(currentPrice, buyer)
    case BidTimer => {

      system.scheduler.scheduleOnce(10 seconds){
        buyer ! AuctionDone(buyer, currentPrice)
        self ! DeleteTimer
      }
      context become Sold
    }
    case DeleteTimer => {}
  }

  def Ignored : Receive = LoggingReceive {
    case Bid(amount) if amount > currentPrice => {
      currentPrice = amount
      buyer ! Current(amount, sender)
      buyer = sender
      system.scheduler.scheduleOnce(10 seconds){
        self ! BidTimer
      }
      context become Activated
    }
    case Bid(amount) => sender ! Current(currentPrice, buyer)

    case DeleteTimer => {
      context stop self
    }
  }

  def Sold : Receive = LoggingReceive {
    case DeleteTimer => {
      context stop self
    }
  }

  override def receive = Created
}


object Message {
  //inside auction messages:

  case object BidTimer
  case object DeleteTimer

  //buyer to auction messages:

  case class Bid(amount: BigInt){
    require(amount > 0)
  }

  //auction to buyer messages:

  case class Current(amount: BigInt, buyer: ActorRef){
    require(amount > 0)
  }

  case class AuctionDone(winner: ActorRef, amount: BigInt)
}
