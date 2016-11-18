import MasterSearch.Terminated
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, RoundRobinRoutingLogic, Router}


object MasterSearch {
  case class Terminated(a: ActorRef);
}

class MasterSearch extends Actor with ActorLogging {
  val nbOfroutees: Int = 5

  val routees = Vector.fill(nbOfroutees) {
    val r = context.actorOf(Props[AuctionSearch])
    context watch r
    ActorRefRoutee(r)
  }

  var broadcastRouter = {
    Router(BroadcastRoutingLogic(), routees)
  }

  var roundRRouter = {
    Router(RoundRobinRoutingLogic(), routees)
  }

  def receive = LoggingReceive {
    case register: Message.RegisterAuction =>
      broadcastRouter.route(register, sender())

    case search: Message.GetAuctions =>
      roundRRouter.route(search, sender())

    case Terminated(a) =>
      broadcastRouter= broadcastRouter.removeRoutee(a)
      if (broadcastRouter.routees.size == 0)
        context.system.terminate

  }
}