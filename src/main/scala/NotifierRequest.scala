import Message.Notify
import NotifierRequest.NotifyException
import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor._

object NotifierRequest {
  def props(notify: Notify): Props =
    Props(classOf[NotifierRequest], notify)

  class NotifyException extends Exception("Notify")

}

class NotifierRequest(notify: Notify) extends Actor {

  val remote = context.actorSelection("akka.tcp://Publisher@127.0.0.1:2553/user/auctionpublisher")
  override val supervisorStrategy = OneForOneStrategy(loggingEnabled = false) {
    case _: NotifyException =>
      Restart
    case _ =>
      Escalate
  }

  override def preStart(): Unit =  {
   {
      remote ! Notify(notify.title, notify.buyer, notify.price)
      context.parent ! "Ok"
      context.stop(self)
    }

  }

  def receive = {
    case Notify(title, buyer, price) => {
      remote ! Notify(title, buyer, price)
      context.parent ! "Ok"
      context.stop(self)
    }

  }


}