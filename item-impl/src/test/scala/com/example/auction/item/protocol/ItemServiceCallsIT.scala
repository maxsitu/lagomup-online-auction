package com.example.auction.item.protocol

import java.time.Duration
import java.util.UUID

import akka.stream.scaladsl.Sink
import com.example.auction.item.api
import com.example.auction.item.api._
import com.example.auction.item.impl.ItemApplication
//import com.example.auction.security.ClientSecurity._
import com.example.auction.utils.ClientSecurity
import com.lightbend.lagom.scaladsl.api.AdditionalConfiguration
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LocalServiceLocator}
import com.lightbend.lagom.scaladsl.testkit.{ServiceTest, TestTopicComponents}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.Configuration
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable.Seq
import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}

class ItemServiceCallsIT extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra()) { ctx =>
    new ItemApplication(ctx) with LocalServiceLocator with TestTopicComponents {

      override def additionalConfiguration: AdditionalConfiguration = {
        super.additionalConfiguration ++ Configuration.from(Map(
          "cassandra-query-journal.eventual-consistency-delay" -> "0"
        ))
      }



    }
  }

  val itemService = server.serviceClient.implement[ItemService]

  import server.materializer

  override def afterAll(): Unit = server.stop()

  "The item service" should {

    "allow creating items" in {
      val creatorId = UUID.randomUUID()
      for {
        created <- createItem(creatorId, sampleItem(creatorId))
        retrieved <- retrieveItem(created.id.get)
      } yield {
        created should ===(retrieved)
      }
    }

    "return all items for a given user" in {
      val tom = UUID.randomUUID
      val jerry = UUID.randomUUID
      val tomItem = sampleItem(tom)
      val jerryItem = sampleItem(jerry)
      (for {
        _ <- createItem(jerry, jerryItem)
        createdTomItem <- createItem(tom, tomItem)
      } yield {
        awaitSuccess() {
          for {
            items <- itemService.getItemsForUser(tom, "Created", None).invoke()
          } yield {
            items.count should ===(1)
            items.items should contain only ItemSummary(createdTomItem.id.getOrElse(UUID.randomUUID()), tomItem.itemData.title, tomItem.itemData.currencyId,
              tomItem.itemData.reservePrice, tomItem.status)
          }
        }
      }).flatMap(identity)
    }

    "emit auction started event" in {
      val creatorId = UUID.randomUUID
      for {
        createdItem <- createItem(creatorId, sampleItem(creatorId))
        _ <- retrieveItem(createdItem.id.get)
        _ <- startAuction(creatorId, createdItem.id.get)
        events: Seq[ItemEvent] <- itemService.itemEvents.subscribe.atMostOnceSource
          .filter(_.itemId == createdItem.id.getOrElse(UUID.randomUUID()))
          .take(2)
          .runWith(Sink.seq)
      } yield {
        events.size shouldBe 2
        events.head shouldBe an[ItemUpdated]
        events.drop(1).head shouldBe an[AuctionStarted]
      }
    }

  }

  // Helpers -----------------------------------------------------------------------------------------------------------


  private def sampleItem(creatorId: UUID) = {
    Item(
      id = None,
      creator = creatorId,
      itemData = ItemData(
        "title", "description", "USD", 10, 10, 10, None
      ),
      price = None,
      status = "Created",
      auctionStart = None, auctionEnd = None, auctionWinner = None
    )
  }

  private def createItem(creatorId: UUID, createItem: Item) = {
    itemService.createItem().handleRequestHeader(ClientSecurity.authenticate(creatorId)).invoke(createItem)
  }

  private def retrieveItem(itemId: UUID) = {
    itemService.getItem(itemId).invoke
  }

  private def startAuction(creatorId: UUID, itemId: UUID) = {
    itemService.startAuction(itemId).handleRequestHeader(ClientSecurity.authenticate(creatorId)).invoke
  }

  private def awaitSuccess[T](maxDuration: FiniteDuration = 10.seconds, checkEvery: FiniteDuration = 100.milliseconds)(block: => Future[T]): Future[T] = {
    val checkUntil = System.currentTimeMillis() + maxDuration.toMillis

    def doCheck(): Future[T] = {
      block.recoverWith {
        case recheck if checkUntil > System.currentTimeMillis() =>
          val timeout = Promise[T]()
          server.application.actorSystem.scheduler.scheduleOnce(checkEvery) {
            timeout.completeWith(doCheck())
          }(server.executionContext)
          timeout.future
      }
    }

    doCheck()
  }

}
