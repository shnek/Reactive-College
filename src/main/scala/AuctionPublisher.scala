import Message.Notify
import akka.actor.Actor

class AuctionPublisher extends Actor{
  override def receive: Receive = {
    case Notify(title, buyer, price) => {
      println(title + " is currently being bid by " + buyer + " for " + price)
    }
  }
}
