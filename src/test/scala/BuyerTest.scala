import Message.{Bid, Current}
import akka.actor._
import akka.testkit.{TestKit, TestProbe}

import scala.concurrent.duration._

class BuyerTest extends TestKit(ActorSystem("Auctions")){
  val probe1 = TestProbe()
  val probe2 = TestProbe()
  val actor = system.actorOf(Props(classOf[Buyer], 10))
  val amount = 5
  actor ! Current(amount, null)
  probe1.expectMsg(500 millis, Bid(amount+1))


}
