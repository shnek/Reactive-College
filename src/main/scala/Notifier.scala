import Message.Notify
import akka.actor.Actor



class Notifier extends Actor {

  val remote = context.actorSelection("akka.tcp://Publisher@127.0.0.1:2553/user/auctionpublisher")

  override def receive: Receive = {
    case Notify(title, buyer, price) => remote ! Notify(title, buyer, price)
  }
}
