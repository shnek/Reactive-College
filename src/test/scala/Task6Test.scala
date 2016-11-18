import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._

class Task6Test extends TestKit(ActorSystem("Auctions"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate
  }


  "finalTest" in {
    val config = ConfigFactory.load()

    val system = ActorSystem("Auctions")
    val serversystem = ActorSystem("Auctions", config.getConfig("serverapp").withFallback(config))
    val remotesystem = ActorSystem("Publisher", config.getConfig("clientapp").withFallback(config))

    serversystem.actorOf(Props[MasterSearch], "masterSearch")
    serversystem.actorOf(Props[Notifier], "notifier")

    val helper = system.actorOf(Props[TestHelper])
    helper ! "start"
    expectMsg(50000 millis, _ : Long)
  }
}