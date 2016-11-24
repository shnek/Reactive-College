import akka.actor.{ActorRef, actorRef2Scala}
import akka.persistence.fsm.PersistentFSM.FSMState
import akka.persistence.fsm._

import scala.concurrent.duration._
import scala.reflect.{ClassTag, _}


sealed trait State extends FSMState

case object Created extends State {
  override def identifier: String = "Created"
}

case object Activated extends State {
  override def identifier: String = "Activated"
}

case object Ignored extends State {
  override def identifier: String = "Ignored"
}

case object Sold extends State {
  override def identifier: String = "Sold"
}

sealed trait Data
case class AuctionData(buyer: ActorRef, currentPrice: BigInt) extends Data

sealed trait DomainEvent
case class ChangeData(actor: ActorRef, amount: BigInt) extends DomainEvent


class Auction extends PersistentFSM[State, Data, DomainEvent] {

  override def persistenceId = "auction-fsm-id-2"
  override def domainEventClassTag: ClassTag[DomainEvent] = classTag[DomainEvent]
  val notifier = context.actorSelection("/user/notifier")


  import Message._
  startWith(Created, AuctionData(self, 0))

  when(Created) {
    case Event(Bid(amount), AuctionData(buyer, currentPrice)) if amount > currentPrice => {
      goto(Activated) applying ChangeData(sender, amount) replying AuctionData(sender, amount)
    }
    case Event(Bid(amount), AuctionData(buyer, currentPrice)) if amount <= currentPrice => {
      sender ! Current(currentPrice, buyer)
      stay
    }
    case Event(BidTimer, _) => {
      goto(Ignored)
    }
  }

  when(Activated) {
    case Event(Bid(amount), AuctionData(buyer, currentPrice)) if amount > currentPrice => {
      buyer ! Current(amount, sender)
      stay applying ChangeData(sender, amount)
    }
    case Event(Bid(amount), AuctionData(buyer, currentPrice)) if amount <= currentPrice => {
      sender ! Current(currentPrice, buyer)
      stay
    }
    case Event(BidTimer, AuctionData(buyer, currentPrice)) => {
      buyer ! AuctionDone(buyer, currentPrice)
      context.parent ! AuctionDone(buyer, currentPrice)
      goto(Sold)
    }
    case Event(DeleteTimer, _) => stay
  }

  when(Ignored) {
    case Event(Bid(amount), AuctionData(buyer, currentPrice)) if amount > currentPrice => {
      buyer ! Current(amount, sender)
      goto(Activated) applying ChangeData(sender, amount)
    }
    case Event(Bid(amount), AuctionData(buyer, currentPrice)) => sender ! Current(currentPrice, buyer); stay
    case Event(DeleteTimer, _) =>
//      println("Auction is dead!");
      stop
  }
  when(Sold) {
    case Event(DeleteTimer, _) => println("Auction is sold!"); stop
  }

  onTransition {
    case x -> Created => setTimer("BidTimer", BidTimer, 50 seconds);
    case Created -> Ignored => setTimer("DeleteTimer", DeleteTimer, 50 seconds);
    case Activated -> Sold => setTimer("DeleteTimer", DeleteTimer, 10 seconds);
    case Ignored -> Created => setTimer("BidTimer", BidTimer, 50 seconds); cancelTimer("DeleteTimer");
  }

  override def applyEvent(event: DomainEvent, currentData: Data): Data = {
    event match {
      case ChangeData(sender, amount) => {
        notifier ! Notify(self.path.name, sender.path.name, amount)
        AuctionData.apply(sender, amount)
      }
    }
  }
}


