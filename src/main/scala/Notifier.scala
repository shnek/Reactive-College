import Message.Notify
import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorRef, OneForOneStrategy}



class Notifier extends Actor {

  import NotifierRequest._
  var pendingWorkers = Map[ActorRef, ActorRef]()


  override val supervisorStrategy = OneForOneStrategy(loggingEnabled = false){
    case e =>
      println("Something went wrong: " + e.getMessage)
      Stop
  }
  override def receive: Receive = {
    case Notify(title, buyer, price) =>
      val worker = context.actorOf(NotifierRequest.props(Notify(title, buyer, price)))
      pendingWorkers += worker -> sender()
    case "Ok" => {}
  }

}
